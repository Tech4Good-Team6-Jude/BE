package com.dobak.backend.dto;

/**
 * 학습 액션(발음연습 통과, 유사문장 완료 등) 한 번으로 알을 얻었을 때 같이 내려주는 정보.
 * "6·리워드"(알 획득 애니메이션)와 "7·부화"(막 부화했을 때) 화면을 이 응답 하나로 그릴 수 있게 한다.
 * eggsGained가 0이면 이번 액션으로는 알을 못 얻은 것(정확도 임계값 미달 등).
 */
public record EggRewardInfo(
        int eggsGained,
        int currentHatchProgress,
        int eggsPerHatch,
        boolean justHatched,
        HatchRewardSummary hatchReward
) {
    public static EggRewardInfo none(int currentHatchProgress, int eggsPerHatch) {
        return new EggRewardInfo(0, currentHatchProgress, eggsPerHatch, false, null);
    }
}
