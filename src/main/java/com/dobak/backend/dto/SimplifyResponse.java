package com.dobak.backend.dto;

import java.util.List;

/**
 * A모드(즉시 보조 모드) 응답.
 * originalText: OCR로 추출된 원본 텍스트
 * simplifiedText: LLM으로 재작성된 쉬운 문장
 * audioUrl: TTS로 생성된 음성 파일 주소
 * words: 낭독 하이라이트용 단어별 타임스탬프 (simplifiedText 기준)
 */
public record SimplifyResponse(
        String originalText,
        String simplifiedText,
        String audioUrl,
        List<WordTimestamp> words
) {
}
