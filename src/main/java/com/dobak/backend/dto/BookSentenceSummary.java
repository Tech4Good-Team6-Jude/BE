package com.dobak.backend.dto;

/** stuck: 이 아이가 이 문장을 이미 "막힌 문장"으로 표시해뒀는지 (L3 재진입 시 표시 복원용) */
public record BookSentenceSummary(Long sentenceId, int orderIndex, String text, boolean stuck) {
}
