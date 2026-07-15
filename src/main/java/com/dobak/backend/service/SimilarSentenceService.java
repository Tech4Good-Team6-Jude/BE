package com.dobak.backend.service;

import com.dobak.backend.dto.EggRewardInfo;
import com.dobak.backend.dto.SimilarSentenceSummary;
import com.dobak.backend.dto.SimilarSetResponse;
import com.dobak.backend.entity.CaptureSentence;
import com.dobak.backend.entity.SimilarSentenceItem;
import com.dobak.backend.entity.SimilarSentenceSet;
import com.dobak.backend.entity.StuckSentence;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.SimilarSentenceGenerationResult;
import com.dobak.backend.repository.CaptureSentenceRepository;
import com.dobak.backend.repository.SimilarSentenceItemRepository;
import com.dobak.backend.repository.SimilarSentenceSetRepository;
import com.dobak.backend.repository.StuckSentenceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 기능명세서 3번(패턴 기반 유사문장 반복학습).
 * 막힌 문장 하나를 두고, 같은 패턴(음소/받침/어휘/문장구조)의 유사문장 3~5개를 생성해서
 * 순서대로 따라읽으며 완료 체크할 수 있게 한다.
 * 막힌 문장의 출처는 두 갈래: 사진 캡처(1번 기능) 흐름의 CaptureSentence, 도서관 흐름의 StuckSentence.
 */
@Service
public class SimilarSentenceService {

    private static final int DEFAULT_COUNT = 4;
    private static final String DEFAULT_DIFFICULTY = "보통";

    private final InferenceClient inferenceClient;
    private final CaptureSentenceRepository captureSentenceRepository;
    private final StuckSentenceRepository stuckSentenceRepository;
    private final SimilarSentenceSetRepository setRepository;
    private final SimilarSentenceItemRepository itemRepository;
    private final ProgressService progressService;

    public SimilarSentenceService(InferenceClient inferenceClient,
                                   CaptureSentenceRepository captureSentenceRepository,
                                   StuckSentenceRepository stuckSentenceRepository,
                                   SimilarSentenceSetRepository setRepository,
                                   SimilarSentenceItemRepository itemRepository,
                                   ProgressService progressService) {
        this.inferenceClient = inferenceClient;
        this.captureSentenceRepository = captureSentenceRepository;
        this.stuckSentenceRepository = stuckSentenceRepository;
        this.setRepository = setRepository;
        this.itemRepository = itemRepository;
        this.progressService = progressService;
    }

    /** 막힌 문장 기반으로 유사문장 세트 생성 (3.1) — 사진 캡처(1번 기능) 흐름 */
    public SimilarSetResponse createSet(Long sentenceId, Integer count, String difficulty) {
        CaptureSentence sentence = captureSentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문장: " + sentenceId));

        int resolvedCount = count != null ? count : DEFAULT_COUNT;
        String resolvedDifficulty = difficulty != null ? difficulty : DEFAULT_DIFFICULTY;

        SimilarSentenceGenerationResult generated =
                generateWithFallback(sentence.getEffectiveText(), resolvedCount, resolvedDifficulty);

        SimilarSentenceSet set = SimilarSentenceSet.fromCaptureSentence(sentence, generated.pattern(), resolvedDifficulty);
        setRepository.save(set);

        return saveItemsAndRespond(set, generated.sentences());
    }

    /** 막힌 문장 기반으로 유사문장 세트 생성 — 도서관(책 읽기) 흐름 (L5 반복학습) */
    public SimilarSetResponse createSetForStuckSentence(Long stuckSentenceId, Integer count, String difficulty) {
        StuckSentence stuckSentence = stuckSentenceRepository.findById(stuckSentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 막힌 문장: " + stuckSentenceId));

        int resolvedCount = count != null ? count : DEFAULT_COUNT;
        String resolvedDifficulty = difficulty != null ? difficulty : DEFAULT_DIFFICULTY;

        SimilarSentenceGenerationResult generated =
                generateWithFallback(stuckSentence.getText(), resolvedCount, resolvedDifficulty);

        SimilarSentenceSet set = SimilarSentenceSet.fromStuckSentence(stuckSentence, generated.pattern(), resolvedDifficulty);
        setRepository.save(set);

        return saveItemsAndRespond(set, generated.sentences());
    }

    /** 세트 재조회 — 화면 새로고침/재진입 시 진행 상태(몇 번째까지 했는지) 복원용 */
    public SimilarSetResponse getSet(Long setId) {
        SimilarSentenceSet set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유사문장 세트: " + setId));
        List<SimilarSentenceItem> items = itemRepository.findBySetIdOrderByOrderIndexAsc(setId);
        return toResponse(set, items, null);
    }

    /**
     * 문장별 완료 체크 (3.2.1). 짧은 긍정 피드백(3.2.2) 차원에서 완료할 때마다 알 +1을 지급하고,
     * 그 결과(알 획득/부화 여부)를 같이 내려줘서 FE가 "6·리워드"/"7·부화" 화면을 바로 띄울 수 있게 한다.
     * 도서관 흐름(stuckSentence 출처)이면, 세트 안 문장을 전부 완료했을 때 막힌 문장을
     * resolved=true로 전환한다 (L6 "막힌 문장 N개 모두 익혔어요" 집계용).
     */
    public SimilarSetResponse completeItem(Long setId, Long itemId) {
        SimilarSentenceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유사문장: " + itemId));
        item.complete();
        itemRepository.save(item);

        SimilarSentenceSet set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유사문장 세트: " + setId));
        List<SimilarSentenceItem> items = itemRepository.findBySetIdOrderByOrderIndexAsc(setId);

        if (set.getStuckSentence() != null && items.stream().allMatch(SimilarSentenceItem::isCompleted)) {
            StuckSentence stuckSentence = set.getStuckSentence();
            stuckSentence.resolve();
            stuckSentenceRepository.save(stuckSentence);
        }

        EggRewardInfo eggReward = progressService.addEgg(resolveChildId(set));

        return toResponse(set, items, eggReward);
    }

    /** 세트가 어느 흐름(사진 캡처/도서관)에서 왔든 상관없이 이 세트가 속한 아이의 id를 찾는다 */
    private Long resolveChildId(SimilarSentenceSet set) {
        if (set.getStuckSentence() != null) {
            return set.getStuckSentence().getChild().getId();
        }
        return set.getCaptureSentence().getSession().getChild().getId();
    }

    /** 생성 실패/지연 시 원문 기반 템플릿으로 대체 (3.1.2) */
    private SimilarSentenceGenerationResult generateWithFallback(String sourceText, int count, String difficulty) {
        try {
            SimilarSentenceGenerationResult result = inferenceClient.generateSimilarSentences(sourceText, count, difficulty);
            if (result == null || result.sentences() == null || result.sentences().isEmpty()) {
                return fallback(sourceText, count);
            }
            return result;
        } catch (Exception e) {
            return fallback(sourceText, count);
        }
    }

    private SimilarSentenceGenerationResult fallback(String sourceText, int count) {
        List<String> template = List.of(sourceText, sourceText, sourceText, sourceText, sourceText)
                .stream().limit(Math.max(count, 1)).toList();
        return new SimilarSentenceGenerationResult("일반", template);
    }

    private SimilarSetResponse saveItemsAndRespond(SimilarSentenceSet set, List<String> sentences) {
        List<SimilarSentenceItem> items = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String text = sentences.get(i);
            String audioUrl = inferenceClient.tts(text).audioUrl();
            items.add(new SimilarSentenceItem(set, i, text, audioUrl));
        }
        itemRepository.saveAll(items);
        return toResponse(set, items, null);
    }

    private SimilarSetResponse toResponse(SimilarSentenceSet set, List<SimilarSentenceItem> items, EggRewardInfo eggReward) {
        List<SimilarSentenceSummary> summaries = items.stream()
                .map(i -> new SimilarSentenceSummary(i.getId(), i.getOrderIndex(), i.getText(), i.getAudioUrl(), i.isCompleted()))
                .toList();
        return new SimilarSetResponse(
                set.getId(), set.getSourceId(), set.getSourceType(), set.getPattern(), set.getDifficulty(),
                summaries, eggReward
        );
    }
}
