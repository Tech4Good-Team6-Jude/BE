package com.dobak.backend.dto;

import java.time.LocalDateTime;

public record PracticeAttemptSummary(Long attemptId, String targetText, double accuracy, LocalDateTime createdAt) {
}
