package com.dobak.backend.inference;

import com.dobak.backend.dto.WordTimestamp;
import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.inference.dto.TtsResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public String ocr(MultipartFile file) {
        // TODO: POST /internal/v1/ocr
        return "원본 텍스트 (OCR 결과가 들어올 자리)";
    }

    @Override
    public String simplify(String originalText) {
        // TODO: POST /internal/v1/simplify
        return "쉬운 문장으로 바뀐 텍스트입니다.";
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
        // TODO: POST /internal/v1/pronunciation-eval
        return new PronunciationEvalResult("나비 구룸 하늘", 0.82, List.of("구름"));
    }
}
