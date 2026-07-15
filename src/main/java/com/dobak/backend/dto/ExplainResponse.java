package com.dobak.backend.dto;

import java.util.List;

/**
 * level 0: originalText == explainedText (원문 그대로 낭독), keyWords는 빈 배열
 * level 1~3: explainedText가 단계별로 더 쉬워진 문장 (originalText는 원문 병기용으로 항상 같이 내려감)
 * words: 낭독 하이라이트 동기화용 단어별 타임스탬프 (explainedText 기준)
 * keyWords: 원문에서 이 재설명이 필요했던 근거가 된 핵심 단어 — FE가 원문 표시할 때 하이라이트용 (기능명세서 2.2.2)
 */
public record ExplainResponse(
        Long queryId,
        Long sentenceId,
        int level,
        String originalText,
        String explainedText,
        List<KeyWordDetail> keyWords,
        String audioUrl,
        List<WordTimestamp> words
) {
}
