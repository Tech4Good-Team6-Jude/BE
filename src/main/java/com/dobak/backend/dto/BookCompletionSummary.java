package com.dobak.backend.dto;

/**
 * L6(완료) 화면용 요약. progress는 세션 한정 값이 아니라 아이의 현재 전체 진행 상태(알/부화판/정확도 평균) 스냅샷.
 * "정확도 변화량"처럼 이 책 읽기 세션에만 한정된 델타는 별도 세션 상태 추적이 필요해 이번 범위에서는 제공하지 않는다.
 */
public record BookCompletionSummary(
        Long bookId,
        int resolvedStuckSentenceCount,
        int totalStuckSentenceCount,
        ProgressResponse progress
) {
}
