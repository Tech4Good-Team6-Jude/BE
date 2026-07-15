package com.dobak.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(
        Long reportId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        String summary,
        List<PracticeAttemptSummary> attempts,
        List<ErrorPatternSummary> errorPatterns
) {
}
