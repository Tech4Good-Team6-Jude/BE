package com.dobak.backend.service;

import com.dobak.backend.dto.ExplainResponse;
import com.dobak.backend.dto.KeyWordDetail;
import com.dobak.backend.dto.WordCardResponse;
import com.dobak.backend.entity.CaptureSentence;
import com.dobak.backend.entity.StuckSentence;
import com.dobak.backend.entity.UnknownWordQuery;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.inference.dto.SimplifyResult;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.inference.dto.WordCardResult;
import com.dobak.backend.repository.CaptureSentenceRepository;
import com.dobak.backend.repository.StuckSentenceRepository;
import com.dobak.backend.repository.UnknownWordQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기능명세서 2번(이해 보조). 문장 하나를 두고 원문 낭독(2.1)부터
 * 단계별 재설명(2.2), 단어 풀이(2.3)까지 담당한다.
 */
@Service
public class ExplainService {

    private final InferenceClient inferenceClient;
    private final CaptureSentenceRepository captureSentenceRepository;
    private final StuckSentenceRepository stuckSentenceRepository;
    private final UnknownWordQueryRepository unknownWordQueryRepository;

    public ExplainService(InferenceClient inferenceClient,
                           CaptureSentenceRepository captureSentenceRepository,
                           StuckSentenceRepository stuckSentenceRepository,
                           UnknownWordQueryRepository unknownWordQueryRepository) {
        this.inferenceClient = inferenceClient;
        this.captureSentenceRepository = captureSentenceRepository;
        this.stuckSentenceRepository = stuckSentenceRepository;
        this.unknownWordQueryRepository = unknownWordQueryRepository;
    }

    /**
     * level 0: 원문을 그대로 TTS 낭독 (하이라이트 동기화용 타이밍 포함) — 2.1
     * level 1~3: 단계적으로 더 쉬운 문장으로 재설명, 원문은 항상 같이 내려줌(원문 병기) — 2.2
     * 사진 캡처(1번 기능) 흐름 전용 — CaptureSentence 기준. 로그(UnknownWordQuery)로 남는다.
     */
    public ExplainResponse explain(Long sentenceId, int level) {
        CaptureSentence sentence = captureSentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문장: " + sentenceId));

        String originalText = sentence.getEffectiveText();
        SimplifyResult simplifyResult = simplify(originalText, level);
        String explainedText = simplifyResult.simplifiedText();
        List<KeyWordDetail> keyWords = simplifyResult.keyWords();

        TtsResult tts = inferenceClient.tts(explainedText);

        List<String> keyWordTexts = keyWords.stream().map(KeyWordDetail::word).toList();
        UnknownWordQuery query = new UnknownWordQuery(sentence, level, explainedText, keyWordTexts, tts.audioUrl());
        unknownWordQueryRepository.save(query);

        return new ExplainResponse(
                query.getId(), sentence.getId(), level,
                originalText, explainedText, keyWords, tts.audioUrl(), tts.words()
        );
    }

    /**
     * 도서관(L5 반복학습 "이해" 탭) 흐름 전용 — 막힌 문장(StuckSentence) 기준.
     * 캡처 흐름과 달리 요청량이 적고 로그 성격이 약해 UnknownWordQuery에는 남기지 않는다(queryId는 null로 내려감).
     */
    public ExplainResponse explainStuckSentence(Long stuckSentenceId, int level) {
        StuckSentence stuckSentence = stuckSentenceRepository.findById(stuckSentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 막힌 문장: " + stuckSentenceId));

        String originalText = stuckSentence.getText();
        SimplifyResult simplifyResult = simplify(originalText, level);
        String explainedText = simplifyResult.simplifiedText();
        List<KeyWordDetail> keyWords = simplifyResult.keyWords();

        TtsResult tts = inferenceClient.tts(explainedText);

        return new ExplainResponse(
                null, stuckSentence.getSentence().getId(), level,
                originalText, explainedText, keyWords, tts.audioUrl(), tts.words()
        );
    }

    private SimplifyResult simplify(String originalText, int level) {
        return level <= 0
                ? new SimplifyResult(originalText, List.of())
                : inferenceClient.simplify(originalText, level);
    }

    /** 문장 내 단어를 탭했을 때 뜻/예문/발음 카드 — 2.3 */
    public WordCardResponse getWordCard(String word) {
        WordCardResult result = inferenceClient.lookupWord(word);
        return new WordCardResponse(result.word(), result.meaning(), result.example(), result.audioUrl());
    }
}
