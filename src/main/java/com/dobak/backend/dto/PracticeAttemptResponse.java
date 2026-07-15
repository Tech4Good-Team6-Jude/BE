package com.dobak.backend.dto;

import java.util.List;

public record PracticeAttemptResponse(
        Long attemptId,
        String sttText,
        double accuracy,
        List<String> misreadWords,
        String feedback
) {
}
