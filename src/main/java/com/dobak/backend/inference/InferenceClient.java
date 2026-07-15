package com.dobak.backend.inference;

import com.dobak.backend.inference.dto.ErrorTypeAnalysisResult;
import com.dobak.backend.inference.dto.PronunciationEvalResult;
import com.dobak.backend.inference.dto.SimilarSentenceGenerationResult;
import com.dobak.backend.inference.dto.SimplifyResult;
import com.dobak.backend.inference.dto.TtsResult;
import com.dobak.backend.inference.dto.WordCardResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service Server(/api/v1, 우리)가 Inference Server(/internal/v1, AI팀)를 호출하는 경계.
 * 지금은 {@link MockInferenceClient}가 이 인터페이스를 구현해서 mock 데이터를 반환한다.
 * AI팀 서버가 준비되면 HttpInferenceClient(RestClient로 /internal/v1/* 호출) 같은 구현체를
 * 추가해서 갈아끼우면 되고, 이 인터페이스를 쓰는 다른 서비스 코드는 건드릴 필요 없음.
 */
public interface InferenceClient {

    /** OCR: 이미지 -> 문장 단위로 분할된 텍스트 목록 (기능명세서 1.2: OCR+문장분할을 AI가 한 번에 처리) */
    List<String> ocr(MultipartFile file);

    /**
     * 문장 단순화: 원본 텍스트 + 난이도(1~3, 클수록 더 쉬움) -> 쉬운 문장 + 근거 단서(핵심 단어들).
     * keyWords는 원문에서 이 재설명이 필요했던 이유가 된 단어 — 기능명세서 2.2.2 "근거 단서 제공".
     */
    SimplifyResult simplify(String originalText, int level);

    /** TTS: 텍스트 -> 오디오 + 단어별 타임스탬프(하이라이트 동기화용) */
    TtsResult tts(String text);

    /** 단어 풀이: 단어 -> 뜻/예문/발음 오디오 */
    WordCardResult lookupWord(String word);

    /** STT: 오디오 -> 텍스트 */
    String stt(MultipartFile audioFile);

    /** 오류 유형 분석: 누적된 "모르는 부분" 텍스트들 -> 오류 유형(공통분모) */
    ErrorTypeAnalysisResult analyzeErrorType(List<String> unknownTexts);

    /** 발음·유창성 평가: 녹음 + 목표 텍스트 -> STT결과/정확도/불일치구간(유형+시범발음 포함) */
    PronunciationEvalResult evaluatePronunciation(MultipartFile audioFile, String targetText);

    /** 유사문장 생성: 막힌 문장 -> 공통 패턴명 + 동일 패턴의 유사문장 목록 */
    SimilarSentenceGenerationResult generateSimilarSentences(String sourceText, int count, String difficulty);

    /**
     * 문장 패턴 분석: 도서관(L3)에서 아이가 "막힌 문장"으로 표시한 문장 하나 -> 소리 패턴 태그
     * (예: "겹받침"/"된소리"/"긴문장"). L4 막힌문장 목록의 태그 표시, 리포트 집계에 쓰인다.
     */
    String analyzeSentencePattern(String text);
}
