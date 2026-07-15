package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 도서관 기능(T2~L6)에서 제공하는 학습 자료(책) 한 권.
 * 해커톤 단계라 title/author/difficulty/totalPages/estimatedMinutes는 시드 데이터로 임의 입력.
 * 실제 본문은 {@link BookSentence}가 페이지·순서 단위로 들고 있다.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private String coverImageUrl;

    /** "쉬움" / "보통" / "어려움" 등 임의값 (기획 확정 전까지 자유 문자열) */
    private String difficulty;

    private int totalPages;

    private int estimatedMinutes;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected Book() {
    }

    public Book(String title, String author, String coverImageUrl, String difficulty,
                int totalPages, int estimatedMinutes) {
        this.title = title;
        this.author = author;
        this.coverImageUrl = coverImageUrl;
        this.difficulty = difficulty;
        this.totalPages = totalPages;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
