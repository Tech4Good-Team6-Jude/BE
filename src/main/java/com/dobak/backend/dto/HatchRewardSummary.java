package com.dobak.backend.dto;

import java.time.LocalDateTime;

/** sentByGuardian: 보호자가 골라서 보낸 리워드인지 (true면 "엄마가 보냈어요" 태그 표시) */
public record HatchRewardSummary(
        Long hatchRewardId,
        String rewardCode,
        String brand,
        String name,
        String imageUrl,
        LocalDateTime issuedAt,
        LocalDateTime validUntil,
        boolean sentByGuardian,
        boolean claimed,
        LocalDateTime claimedAt
) {
}
