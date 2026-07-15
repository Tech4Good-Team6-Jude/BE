package com.dobak.backend.dto;

public record SimilarSentenceSummary(Long itemId, int orderIndex, String text, String audioUrl, boolean completed) {
}
