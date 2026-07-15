package com.dobak.backend.service;

import com.dobak.backend.dto.BookCompletionSummary;
import com.dobak.backend.dto.BookPageResponse;
import com.dobak.backend.dto.BookSentenceSummary;
import com.dobak.backend.dto.BookSummary;
import com.dobak.backend.dto.MarkStuckResponse;
import com.dobak.backend.dto.StuckSentenceSummary;
import com.dobak.backend.entity.Book;
import com.dobak.backend.entity.BookSentence;
import com.dobak.backend.entity.StuckSentence;
import com.dobak.backend.entity.User;
import com.dobak.backend.inference.InferenceClient;
import com.dobak.backend.repository.BookRepository;
import com.dobak.backend.repository.BookSentenceRepository;
import com.dobak.backend.repository.StuckSentenceRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 도서관 기능(T2 목록 ~ L6 완료). 책/책문장은 시드 데이터로 미리 채워두고(BookDataSeeder),
 * 여기서는 조회 + "막힌 문장" 표시/해제만 담당한다. 반복학습(L5)은 SimilarSentenceService가 이어받는다.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookSentenceRepository bookSentenceRepository;
    private final StuckSentenceRepository stuckSentenceRepository;
    private final UserRepository userRepository;
    private final InferenceClient inferenceClient;
    private final ProgressService progressService;

    public BookService(BookRepository bookRepository,
                        BookSentenceRepository bookSentenceRepository,
                        StuckSentenceRepository stuckSentenceRepository,
                        UserRepository userRepository,
                        InferenceClient inferenceClient,
                        ProgressService progressService) {
        this.bookRepository = bookRepository;
        this.bookSentenceRepository = bookSentenceRepository;
        this.stuckSentenceRepository = stuckSentenceRepository;
        this.userRepository = userRepository;
        this.inferenceClient = inferenceClient;
        this.progressService = progressService;
    }

    /** T2: 도서관 목록 */
    public List<BookSummary> listBooks(Long childId) {
        return bookRepository.findAllByOrderByIdAsc().stream()
                .map(book -> toSummary(book, childId))
                .toList();
    }

    /** L2: 책 상세 */
    public BookSummary getBook(Long bookId, Long childId) {
        return toSummary(findBook(bookId), childId);
    }

    /** L3: 읽기 화면 — 페이지 단위로 문장 목록 조회 */
    public BookPageResponse getPage(Long bookId, int pageIndex, Long childId) {
        Book book = findBook(bookId);
        List<BookSentence> sentences = bookSentenceRepository
                .findByBookIdAndPageIndexOrderByOrderIndexAsc(bookId, pageIndex);
        if (sentences.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 페이지: book=" + bookId + ", page=" + pageIndex);
        }

        Set<Long> stuckSentenceIds = stuckSentenceRepository.findByChildIdAndBookIdOrderByCreatedAtAsc(childId, bookId)
                .stream()
                .map(s -> s.getSentence().getId())
                .collect(Collectors.toSet());

        List<BookSentenceSummary> summaries = sentences.stream()
                .map(s -> new BookSentenceSummary(s.getId(), s.getOrderIndex(), s.getText(), stuckSentenceIds.contains(s.getId())))
                .toList();

        String pageImageUrl = sentences.get(0).getPageImageUrl();
        return new BookPageResponse(book.getId(), pageIndex, book.getTotalPages(), pageImageUrl, summaries);
    }

    /**
     * L3: 문장을 탭해서 "막힌 문장"으로 표시/해제 토글.
     * 새로 표시하는 경우에만 Inference(Mock)로 패턴을 분석해서 태그를 붙인다.
     */
    public MarkStuckResponse toggleStuck(Long sentenceId, Long childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));
        BookSentence sentence = bookSentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문장: " + sentenceId));

        return stuckSentenceRepository.findByChildIdAndSentenceId(childId, sentenceId)
                .map(existing -> {
                    stuckSentenceRepository.delete(existing);
                    return new MarkStuckResponse(false, null);
                })
                .orElseGet(() -> {
                    String pattern = inferenceClient.analyzeSentencePattern(sentence.getText());
                    StuckSentence stuckSentence = new StuckSentence(child, sentence.getBook(), sentence, sentence.getText(), pattern);
                    stuckSentenceRepository.save(stuckSentence);
                    return new MarkStuckResponse(true, toSummary(stuckSentence));
                });
    }

    /** L4: 막힌 문장 목록 */
    public List<StuckSentenceSummary> listStuckSentences(Long bookId, Long childId) {
        findBook(bookId); // 존재 검증
        return stuckSentenceRepository.findByChildIdAndBookIdOrderByCreatedAtAsc(childId, bookId).stream()
                .map(this::toSummary)
                .toList();
    }

    /** L6: 완료 화면 요약 */
    public BookCompletionSummary getCompletionSummary(Long bookId, Long childId) {
        findBook(bookId); // 존재 검증
        long total = stuckSentenceRepository.countByChildIdAndBookId(childId, bookId);
        long resolved = stuckSentenceRepository.countByChildIdAndBookIdAndResolvedTrue(childId, bookId);
        return new BookCompletionSummary(bookId, (int) resolved, (int) total, progressService.getProgress(childId));
    }

    private Book findBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책: " + bookId));
    }

    private BookSummary toSummary(Book book, Long childId) {
        long stuckCount = stuckSentenceRepository.countByChildIdAndBookId(childId, book.getId())
                - stuckSentenceRepository.countByChildIdAndBookIdAndResolvedTrue(childId, book.getId());
        return new BookSummary(
                book.getId(), book.getTitle(), book.getAuthor(), book.getCoverImageUrl(),
                book.getDifficulty(), book.getTotalPages(), book.getEstimatedMinutes(), (int) stuckCount
        );
    }

    private StuckSentenceSummary toSummary(StuckSentence s) {
        return new StuckSentenceSummary(s.getId(), s.getSentence().getId(), s.getText(), s.getPattern(), s.isResolved(), s.getCreatedAt());
    }
}
