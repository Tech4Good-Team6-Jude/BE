package com.dobak.backend.inference;

import com.dobak.backend.dto.KeyWordDetail;
import com.dobak.backend.dto.MismatchSegment;
import com.dobak.backend.dto.WordTimestamp;
import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.inference.dto.SimilarSentenceGenerationResult;
import com.dobak.backend.inference.dto.SimplifyResult;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.inference.dto.WordCardResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * AI팀 Inference Server(/internal/v1)가 준비되기 전까지 쓰는 mock 구현체.
 * TODO(AI팀 연동): 각 메서드를 HTTP 호출(RestClient/WebClient)로 교체.
 * 실제 계약(요청/응답 필드)이 확정되면 이 클래스 대신 HttpInferenceClient를 만들어서
 * @Primary 붙이고 이 클래스는 로컬 개발/데모 fallback용으로 남겨두면 됨.
 */
@Service
public class MockInferenceClient implements InferenceClient {

    @Override
    public List<String> ocr(MultipartFile file) {
        // TODO: POST /internal/v1/ocr (이미지 -> 문장 단위 분할된 텍스트 목록)
        return List.of(
                "옛날 옛적, 넓적한 바위 위에 앉아 있던 개구리가",
                "훌쩍 뛰어올랐어요.",
                "연못 건너편에는 낡은 나무 다리가 놓여 있었죠."
        );
    }

    @Override
    public SimplifyResult simplify(String originalText, int level) {
        // TODO: POST /internal/v1/simplify (level 클수록 더 쉬운 문장, keyWords는 AI가 실제로
        // "이 단어 때문에 어려웠다"고 판단한 근거를 내려주는 영역 — 지금은 원문에서 가장 긴 단어를
        // 그럴듯한 후보로 추정하는 mock 규칙만 씀)
        String simplifiedText = switch (level) {
            case 1 -> "약간 쉬운 문장으로 바뀐 텍스트입니다.";
            case 2 -> "더 쉬운 문장으로 바뀐 텍스트입니다.";
            default -> "아주 쉬운 문장으로 바뀐 텍스트입니다.";
        };
        List<KeyWordDetail> keyWords = guessKeyWords(originalText, 1);
        return new SimplifyResult(simplifiedText, keyWords);
    }

    /** mock: 원문에서 가장 긴 단어(들)를 "이 재설명의 근거가 된 핵심 단어"로 추정하고, 뜻은 가짜로 채운다 */
    private List<KeyWordDetail> guessKeyWords(String text, int count) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.trim().split("\\s+"))
                .map(token -> token.replaceAll("[.,!?~'\"]+$", "").replaceAll("^['\"]+", ""))
                .filter(token -> !token.isBlank())
                .sorted((a, b) -> b.length() - a.length())
                .limit(count)
                .map(word -> new KeyWordDetail(word, word + "의 뜻(mock)"))
                .toList();
    }

    @Override
    public TtsResult tts(String text) {
        // TODO: POST /internal/v1/tts (word-level timestamp 지원 여부 확인 필요)
        List<WordTimestamp> words = List.of(
                new WordTimestamp("쉬운", 0, 400),
                new WordTimestamp("문장으로", 400, 900),
                new WordTimestamp("바뀐", 900, 1200),
                new WordTimestamp("텍스트입니다.", 1200, 1800)
        );
        return new TtsResult("https://example.com/mock-audio.mp3", words);
    }

    @Override
    public String stt(MultipartFile audioFile) {
        // TODO: POST /internal/v1/stt
        return "나비 구름 하늘";
    }

    @Override
    public ErrorTypeAnalysisResult analyzeErrorType(List<String> unknownTexts) {
        // TODO: POST /internal/v1/error-analysis
        return new ErrorTypeAnalysisResult(
                "phonological",
                "음운 처리에 어려움이 관찰됩니다. 음운 인식 훈련부터 시작하는 것을 권장해요."
        );
    }

    @Override
    public PronunciationEvalResult evaluatePronunciation(MultipartFile audioFile, String targetText) {
        // TODO: POST /internal/v1/pronunciation-eval (pattern도 AI가 목표 문장 보고 판단해서 내려줌)
        List<MismatchSegment> mismatches = List.of(
                new MismatchSegment("구름", "구룸", "받침", "https://example.com/mock-word-audio-구름.mp3")
        );
        return new PronunciationEvalResult("나비 구룸 하늘", 0.82, mismatches, "겹받침");
    }

    @Override
    public WordCardResult lookupWord(String word) {
        // TODO: POST /internal/v1/word-card
        return new WordCardResult(word, word + "의 뜻(mock)", word + "가 들어간 예문입니다.",
                "https://example.com/mock-word-audio.mp3");
    }

    @Override
    public SimilarSentenceGenerationResult generateSimilarSentences(String sourceText, int count, String difficulty) {
        // TODO: POST /internal/v1/similar-sentences
        List<String> pool = List.of(
                "넓적한 돌을 밟았다.",
                "책을 읽고 앉았다.",
                "값이 너무 비싸다.",
                "여덟 개를 세었다.",
                "닭이 마당을 걸었다."
        );
        List<String> sentences = pool.stream().limit(Math.max(count, 1)).toList();
        return new SimilarSentenceGenerationResult("겹받침", sentences);
    }

    /** 종성(받침) 인덱스 중 "겹받침"(두 자음이 겹쳐진 받침)에 해당하는 것들. 유니코드 한글 음절 분해 공식 기준. */
    private static final int[] DOUBLE_BATCHIM_JONGSEONG_INDEX = {3, 5, 6, 9, 10, 11, 12, 13, 14, 15, 18};
    /** 초성 인덱스 중 "된소리"(쌍자음)에 해당하는 것들 */
    private static final int[] TENSE_CHOSEONG_INDEX = {1, 4, 8, 10, 13};
    private static final int LONG_SENTENCE_LENGTH = 18;

    @Override
    public String analyzeSentencePattern(String text) {
        // TODO: POST /internal/v1/sentence-pattern (실제로는 AI가 음운/형태 분석해서 판단할 영역)
        // 지금은 mock이라 유니코드 한글 음절 분해로 겹받침/된소리 유무를 직접 계산해 태그를 매긴다.
        if (text == null || text.isBlank()) {
            return "일반";
        }
        if (text.length() >= LONG_SENTENCE_LENGTH) {
            return "긴문장";
        }

        int doubleBatchimCount = 0;
        int tenseConsonantCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 0xAC00 || ch > 0xD7A3) {
                continue; // 완성형 한글 음절이 아니면(공백/자모/기타 문자) 건너뜀
            }
            int syllableIndex = ch - 0xAC00;
            int choseong = syllableIndex / (21 * 28);
            int jongseong = syllableIndex % 28;

            if (contains(TENSE_CHOSEONG_INDEX, choseong)) {
                tenseConsonantCount++;
            }
            if (contains(DOUBLE_BATCHIM_JONGSEONG_INDEX, jongseong)) {
                doubleBatchimCount++;
            }
        }

        if (doubleBatchimCount == 0 && tenseConsonantCount == 0) {
            return "일반";
        }
        return doubleBatchimCount >= tenseConsonantCount ? "겹받침" : "된소리";
    }

    private boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
