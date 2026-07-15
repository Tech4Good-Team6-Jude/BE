package com.dobak.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * weeklyEggGain: 이 기간 동안 새로 얻은 알 개수 (accuracy가 알 지급 기준을 넘은 발음점검 시도 수)
 * errorPatterns: 이 기간 발음점검 시도들을 패턴별(겹받침/된소리/긴문장 등)로 집계한 것 ("자주 막힌 유형")
 */
public record ReportResponse(
        Long reportId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        String highlight,
        String summary,
        double accuracy,
        double previousAccuracy,
        double accuracyDeltaPercentagePoint,
        int learningSessionCount,
        int additionalSentenceCount,
        int weeklyEggGain,
        List<PracticeAttemptSummary> attempts,
        List<ErrorPatternSummary> errorPatterns,
        PronunciationComparison pronunciationComparison,
        boolean stampSent
) {
}
