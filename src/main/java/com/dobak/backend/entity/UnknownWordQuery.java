package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 아이가 읽다가 드래그로 선택한 "모르는 부분"과 그 설명(TTS 포함) 기록.
 * 이 데이터가 쌓여서 ErrorPattern(공통분모 학습)의 원재료가 됨.
 */
@Entity
@Table(name = "unknown_word_queries")
public class UnknownWordQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ReadingSession session;

    private String selectedText;

    private String explanation; // 쉬운 문장으로 재설명한 내용

    private String audioUrl; // TTS 결과

    private LocalDateTime createdAt = LocalDateTime.now();

    protected UnknownWordQuery() {
    }

    public UnknownWordQuery(ReadingSession session, String selectedText, String explanation, String audioUrl) {
        this.session = session;
        this.selectedText = selectedText;
        this.explanation = explanation;
        this.audioUrl = audioUrl;
    }

    public Long getId() {
        return id;
    }

    public ReadingSession getSession() {
        return session;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
