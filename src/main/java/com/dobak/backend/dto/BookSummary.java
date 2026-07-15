package com.dobak.backend.dto;

/**
 * 도서관 목록(T2)/책 상세(L2) 공용 응답.
 * stuckSentenceCount: 이 아이가 이 책에서 아직 안 풀고 남겨둔 막힌 문장 개수
 * ("다시 읽으며 학습할 수 있는 막힌 문장 N개" 배지에 사용).
 */
public record BookSummary(
        Long bookId,
        String title,
        String author,
        String coverImageUrl,
        String difficulty,
        int totalPages,
        int estimatedMinutes,
        int stuckSentenceCount
) {
}
