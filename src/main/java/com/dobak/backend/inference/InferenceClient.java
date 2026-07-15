package com.dobak.backend.inference;

import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.inference.dto.TtsResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service Server(/api/v1, 우리)가 Inference Server(/internal/v1, AI팀)를 호출하는 경계.
 * 지금은 {@link MockInferenceClient}가 이 인터페이스를 구현해서 mock 데이터를 반환한다.
 * AI팀 서버가 준비되면 HttpInferenceClient(RestClient로 /internal/v1/* 호출) 같은 구현체를
 * 추가해서 갈아끼우면 되고, 이 인터페이스를 쓰는 다른 서비스 코드는 건드릴 필요 없음.
 */
public interface InferenceClient {

    /** OCR: 이미지 -> 텍스트 */
    String ocr(MultipartFile file);

    /** 문장 단순화: 원본 텍스트 -> 쉬운 문장 */
    String simplify(String originalText);

    /** TTS: 텍스트 -> 오디오 + 단어별 타임스탬프 */
    TtsResult tts(String text);

    /** STT: 오디오 -> 텍스트 */
    String stt(MultipartFile audioFile);

    /** 오류 유형 분석: 누적된 "모르는 부분" 텍스트들 -> 오류 유형(공통분모) */
    ErrorTypeAnalysisResult analyzeErrorType(List<String> unknownTexts);

    /** 발음·유창성 평가: 녹음 + 목표 텍스트 -> STT결과/정확도/오독단어 */
    PronunciationEvalResult evaluatePronunciation(MultipartFile audioFile, String targetText);
}
