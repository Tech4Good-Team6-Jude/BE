package com.dobak.backend.dto;

import java.util.List;

public record ReadingScoreResponse(double accuracy, List<String> misreadWords, String feedback) {
}
