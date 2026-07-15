package com.dobak.backend.repository;

import com.dobak.backend.entity.BookSentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookSentenceRepository extends JpaRepository<BookSentence, Long> {
    List<BookSentence> findByBookIdAndPageIndexOrderByOrderIndexAsc(Long bookId, int pageIndex);

    List<BookSentence> findByBookIdOrderByPageIndexAscOrderIndexAsc(Long bookId);
}
