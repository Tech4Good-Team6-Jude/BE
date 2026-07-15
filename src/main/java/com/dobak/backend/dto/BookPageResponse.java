package com.dobak.backend.dto;

import java.util.List;

public record BookPageResponse(
        Long bookId,
        int pageIndex,
        int totalPages,
        String pageImageUrl,
        List<BookSentenceSummary> sentences
) {
}
