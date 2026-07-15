package com.dobak.backend.dto;

/** 보호자가 다음 부화 때 보낼 리워드를 미리 고를 때 쓰는 요청 (guardianId는 "엄마가 보냈어요" 태그용) */
public record RewardSelectionRequest(Long guardianId, String rewardCode) {
}
