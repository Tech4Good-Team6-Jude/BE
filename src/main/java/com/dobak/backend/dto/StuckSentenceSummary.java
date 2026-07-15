package com.dobak.backend.dto;

import java.time.LocalDateTime;

/**
 * pattern: Inference Server(Mock)가 분석한 소리 패턴 태그 — "겹받침"/"된소리"/"긴문장" 등 (L4 태그)
 * resolved: 반복학습(유사문장 세트)을 완료해서 이미 익힌 문장인지 (L6 집계용)
 */
public record StuckSentenceSummary(
        Long stuckSentenceId,
        Long sentenceId,
        String text,
        String pattern,
        boolean resolved,
        LocalDateTime createdAt
) {
}
