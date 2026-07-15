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
 * A모드: 아이가 자료(책/사진/PDF)를 읽는 세션 단위.
 */
@Entity
@Table(name = "reading_sessions")
public class ReadingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    @Lob
    @Column(length = 10000)
    private String sourceText; // OCR로 추출된 원본 텍스트

    @Lob
    @Column(length = 10000)
    private String simplifiedText; // LLM으로 재작성된 쉬운 문장

    private String audioUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected ReadingSession() {
    }

    public ReadingSession(User child, String sourceText, String simplifiedText, String audioUrl) {
        this.child = child;
        this.sourceText = sourceText;
        this.simplifiedText = simplifiedText;
        this.audioUrl = audioUrl;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getSimplifiedText() {
        return simplifiedText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
