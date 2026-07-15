package com.dobak.backend.inference.dto;

import java.util.List;

public record SimilarSentenceGenerationResult(String pattern, List<String> sentences) {
}
