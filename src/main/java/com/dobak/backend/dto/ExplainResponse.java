package com.dobak.backend.dto;

public record ExplainResponse(Long queryId, String selectedText, String explanation, String audioUrl) {
}
