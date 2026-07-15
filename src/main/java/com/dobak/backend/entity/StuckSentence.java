package com.dobak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 도서관(L3 읽기)에서 아이가 "막혔다"고 표시한 문장 하나.
 * 같은 문장이라도 아이마다, 책마다 막히는 지점이 다르므로 child+book+sentence로 매핑해서 저장한다.
 * L4(막힌문장 목록)는 book+child로 조회하고, L5(반복학습)에서 유사문장 세트를 다 풀면
 * resolved=true로 바뀌어 L6(완료) 집계에 반영된다.
 */
@Entity
@Table(name = "stuck_sentences")
public class StuckSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    @ManyToOne
    private Book book;

    @ManyToOne
    private BookSentence sentence;

    /** 선택 시점의 문장 스냅샷 (BookSentence.text와 보통 동일, 추후 본문이 바뀌어도 안전하게 별도 보관) */
    @Lob
    @Column(length = 1000)
    private String text;

    /** Inference Server(Mock)가 분석한 소리 패턴 태그 — "겹받침"/"된소리"/"긴문장" 등. L4 태그 표시용. */
    private String pattern;

    /** 반복학습(SimilarSentenceSet)을 완료하면 true로 전환 — L6 "막힌 문장 N개 모두 익혔어요" 집계용 */
    private boolean resolved = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected StuckSentence() {
    }

    public StuckSentence(User child, Book book, BookSentence sentence, String text, String pattern) {
        this.child = child;
        this.book = book;
        this.sentence = sentence;
        this.text = text;
        this.pattern = pattern;
    }

    public void resolve() {
        this.resolved = true;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public Book getBook() {
        return book;
    }

    public BookSentence getSentence() {
        return sentence;
    }

    public String getText() {
        return text;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isResolved() {
        return resolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
