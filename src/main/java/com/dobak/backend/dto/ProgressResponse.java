package com.dobak.backend.dto;

/**
 * eggCount 등 4개 = "알" (학습하면 자동 누적, 10개마다 부화)
 * stampCount = "칭찬도장" (보호자가 보낼 때마다 누적, 부화 같은 기준치 없음)
 * averageAccuracy = 전체 기간 발음 정확도 평균 (매 발음점검마다 누적 갱신되는 캐시값)
 * selectedRewardCode = 다음 부화 때 지급될 리워드 (보호자가 미리 골라둔 것, 기본값 "honeycombo")
 */
public record ProgressResponse(
        int eggCount,
        int currentHatchProgress,
        int totalHatchesCompleted,
        int eggsPerHatch,
        int stampCount,
        double averageAccuracy,
        String selectedRewardCode
) {
}
