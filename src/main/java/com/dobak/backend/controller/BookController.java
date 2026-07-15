package com.dobak.backend.controller;

import com.dobak.backend.dto.BookCompletionSummary;
import com.dobak.backend.dto.BookPageResponse;
import com.dobak.backend.dto.BookSummary;
import com.dobak.backend.dto.MarkStuckRequest;
import com.dobak.backend.dto.MarkStuckResponse;
import com.dobak.backend.dto.StuckSentenceSummary;
import com.dobak.backend.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 도서관 기능 (T2 목록 ~ L6 완료). L5(반복학습)는 SimilarSentenceController가 이어받는다. */
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** T2: 도서관 목록 */
    @GetMapping
    public List<BookSummary> listBooks(@RequestParam Long childId) {
        return bookService.listBooks(childId);
    }

    /** L2: 책 상세 */
    @GetMapping("/{bookId}")
    public BookSummary getBook(@PathVariable Long bookId, @RequestParam Long childId) {
        return bookService.getBook(bookId, childId);
    }

    /** L3: 읽기 화면 — 페이지(1부터 시작) 단위 문장 조회 */
    @GetMapping("/{bookId}/pages/{pageIndex}")
    public BookPageResponse getPage(@PathVariable Long bookId, @PathVariable int pageIndex,
                                     @RequestParam Long childId) {
        return bookService.getPage(bookId, pageIndex, childId);
    }

    /** L3: 문장 탭 -> 막힌 문장으로 표시/해제 토글 */
    @PostMapping("/sentences/{sentenceId}/stuck")
    public MarkStuckResponse toggleStuck(@PathVariable Long sentenceId, @RequestBody MarkStuckRequest request) {
        return bookService.toggleStuck(sentenceId, request.childId());
    }

    /** L4: 이 책에서 이 아이가 표시한 막힌 문장 목록 */
    @GetMapping("/{bookId}/stuck-sentences")
    public List<StuckSentenceSummary> listStuckSentences(@PathVariable Long bookId, @RequestParam Long childId) {
        return bookService.listStuckSentences(bookId, childId);
    }

    /** L6: 완료 화면 요약 */
    @GetMapping("/{bookId}/completion-summary")
    public BookCompletionSummary getCompletionSummary(@PathVariable Long bookId, @RequestParam Long childId) {
        return bookService.getCompletionSummary(bookId, childId);
    }
}
