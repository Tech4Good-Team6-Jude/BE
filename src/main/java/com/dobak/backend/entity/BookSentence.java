package com.dobak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 책(Book) 한 권을 페이지 단위, 페이지 안에서는 문장 단위로 쪼갠 본문 한 조각.
 * L3 읽기 화면에서 pageIndex로 페이지를 넘기고, 그 안의 문장 중 하나를 선택해
 * "막힌 문장"(StuckSentence)으로 표시할 수 있다.
 */
@Entity
@Table(name = "book_sentences")
public class BookSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Book book;

    /** 책 속 쪽 번호 (1부터 시작, L3의 "3/12쪽") */
    private int pageIndex;

    /** 같은 페이지 안에서의 문장 순서 (0부터 시작) */
    private int orderIndex;

    @Lob
    @Column(length = 1000)
    private String text;

    /** 페이지 삽화 (optional, 페이지의 첫 문장에만 채워둬도 됨) */
    private String pageImageUrl;

    protected BookSentence() {
    }

    public BookSentence(Book book, int pageIndex, int orderIndex, String text, String pageImageUrl) {
        this.book = book;
        this.pageIndex = pageIndex;
        this.orderIndex = orderIndex;
        this.text = text;
        this.pageImageUrl = pageImageUrl;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getText() {
        return text;
    }

    public String getPageImageUrl() {
        return pageImageUrl;
    }
}
