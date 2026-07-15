package com.dobak.backend.dto;

import java.util.List;

/**
 * 아이 본인용 "나의 읽기" 화면 전용 응답. 보호자용 ReportResponse와 달리
 * 매번 생성 스냅샷을 저장하지 않고 항상 최신 상태를 계산해서 보여준다.
 */
public record ChildReportResponse(
        int sentencesRead,
        double accuracy,
        double previousAccuracy,
        double accuracyDeltaPercentagePoint,
        int streakDays,
        int eggCount,
        int currentHatchProgress,
        int eggsPerHatch,
        List<ErrorPatternSummary> patternCounts,
        List<DayCheckIn> weeklyCheckIns,
        String encouragementMessage
) {
}
