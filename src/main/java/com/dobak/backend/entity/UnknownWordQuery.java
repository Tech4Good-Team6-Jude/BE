package com.dobak.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 이해보조(explain) 호출 기록. 문장 하나(CaptureSentence)에 대해 원문 낭독(level 0)부터
 * 단계별 재설명(level 1~3)까지 몇 번, 어느 단계까지 요청했는지 로그로 남는다.
 * 이 데이터가 쌓여서 세션 로그(5.1.1)의 "사용 기능=재설명" 항목이 된다.
 */
@Entity
@Table(name = "unknown_word_queries")
public class UnknownWordQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CaptureSentence sentence;

    /** 0 = 원문 그대로 낭독, 1~3 = 단계별 재설명 (숫자가 클수록 더 쉬움) */
    private int level;

    @Lob
    @Column(length = 2000)
    private String explainedText; // level 0이면 원문과 동일, 1~3이면 단순화된 텍스트

    /**
     * 원문에서 이 재설명이 필요했던 근거가 된 핵심 단어들 — 기능명세서 2.2.2 "근거 단서 제공".
     * level 0(원문 그대로 낭독)이면 비워둠.
     */
    @ElementCollection
    @CollectionTable(name = "unknown_word_query_key_words", joinColumns = @JoinColumn(name = "unknown_word_query_id"))
    @Column(name = "key_word")
    private List<String> keyWords = new ArrayList<>();

    private String audioUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected UnknownWordQuery() {
    }

    public UnknownWordQuery(CaptureSentence sentence, int level, String explainedText, List<String> keyWords, String audioUrl) {
        this.sentence = sentence;
        this.level = level;
        this.explainedText = explainedText;
        this.keyWords = keyWords != null ? keyWords : new ArrayList<>();
        this.audioUrl = audioUrl;
    }

    public Long getId() {
        return id;
    }

    public CaptureSentence getSentence() {
        return sentence;
    }

    public int getLevel() {
        return level;
    }

    public String getExplainedText() {
        return explainedText;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
