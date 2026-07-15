package com.dobak.backend.dto;

import java.time.LocalDateTime;

public record ReportSummary(
        Long reportId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        String highlight,
        boolean stampSent
) {
}