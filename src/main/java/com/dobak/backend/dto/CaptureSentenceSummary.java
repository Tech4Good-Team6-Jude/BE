package com.dobak.backend.dto;

/**
 * text: 수정본이 있으면 수정본, 없으면 OCR 원본 (CaptureSentence.getEffectiveText())
 */
public record CaptureSentenceSummary(Long sentenceId, int orderIndex, String text) {
}
