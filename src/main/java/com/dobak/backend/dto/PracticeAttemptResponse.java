package com.dobak.backend.dto;

import java.util.List;

/**
 * passed: 관대한 채점 임계값(4.2.2) 통과 여부 — 좌절감을 줄이기 위해 알 지급 기준(0.8)보다 낮게 잡음
 * mismatches: 불일치 단어별 교정 유형 + 시범발음(오류구간만 재생용)
 * eggReward: 이번 시도로 알을 얻었는지/방금 부화했는지 — "6·리워드"/"7·부화" 화면 트리거용
 */
public record PracticeAttemptResponse(
        Long attemptId,
        String sttText,
        double accuracy,
        boolean passed,
        List<MismatchSegment> mismatches,
        String feedback,
        EggRewardInfo eggReward
) {
}
