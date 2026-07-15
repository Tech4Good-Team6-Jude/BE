package com.dobak.backend.dto;

import java.util.List;

/**
 * A모드(즉시 보조 모드) 응답.
 * sessionId: 생성된 ReadingSession id (드래그 설명 요청 시 이 id를 참조)
 * originalText: OCR로 추출된 원본 텍스트
 * simplifiedText: LLM으로 재작성된 쉬운 문장
 * audioUrl: TTS로 생성된 음성 파일 주소
 * words: 낭독 하이라이트용 단어별 타임스탬프 (simplifiedText 기준)
 */
public record SimplifyResponse(
        Long sessionId,
        String originalText,
        String simplifiedText,
        String audioUrl,
        List<WordTimestamp> words
) {
}
