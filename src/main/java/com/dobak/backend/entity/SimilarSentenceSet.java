package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 막힌 문장 하나를 기반으로 생성한 유사문장 세트.
 * pattern: AI가 원문에서 뽑아낸 공통 패턴(예: 겹받침, 된소리, 긴문장 등)
 *
 * 막힌 문장의 출처가 두 갈래라 둘 다 nullable로 두고 하나만 채운다:
 *  - captureSentence: 사진 캡처(1번 기능) 흐름에서 재설명 요청하다 막힌 문장
 *  - stuckSentence: 도서관(책 읽기) 흐름에서 아이가 직접 "막혔다"고 표시한 문장
 */
@Entity
@Table(name = "similar_sentence_sets")
public class SimilarSentenceSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CaptureSentence captureSentence;

    @ManyToOne
    private StuckSentence stuckSentence;

    private String pattern;

    private String difficulty; // "쉬움" / "보통"

    private LocalDateTime createdAt = LocalDateTime.now();

    protected SimilarSentenceSet() {
    }

    private SimilarSentenceSet(CaptureSentence captureSentence, StuckSentence stuckSentence,
                                String pattern, String difficulty) {
        this.captureSentence = captureSentence;
        this.stuckSentence = stuckSentence;
        this.pattern = pattern;
        this.difficulty = difficulty;
    }

    /** 사진 캡처(1번 기능) 흐름에서 막힌 문장 기반으로 세트를 만들 때 */
    public static SimilarSentenceSet fromCaptureSentence(CaptureSentence sentence, String pattern, String difficulty) {
        return new SimilarSentenceSet(sentence, null, pattern, difficulty);
    }

    /** 도서관(책 읽기) 흐름에서 막힌 문장 기반으로 세트를 만들 때 */
    public static SimilarSentenceSet fromStuckSentence(StuckSentence sentence, String pattern, String difficulty) {
        return new SimilarSentenceSet(null, sentence, pattern, difficulty);
    }

    /** 이 세트를 만든 원본 문장의 id (출처 상관없이) — 응답 DTO용 */
    public Long getSourceId() {
        if (captureSentence != null) {
            return captureSentence.getId();
        }
        if (stuckSentence != null) {
            return stuckSentence.getId();
        }
        return null;
    }

    /** 이 세트의 출처 — "CAPTURE"(사진 캡처) / "BOOK"(도서관) */
    public String getSourceType() {
        if (captureSentence != null) {
            return "CAPTURE";
        }
        if (stuckSentence != null) {
            return "BOOK";
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public CaptureSentence getCaptureSentence() {
        return captureSentence;
    }

    public StuckSentence getStuckSentence() {
        return stuckSentence;
    }

    public String getPattern() {
        return pattern;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
