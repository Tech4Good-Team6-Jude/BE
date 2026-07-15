package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 아이별로 누적된 오류 유형(공통분모). 5문항 진단 결과 + 지속적인 UnknownWordQuery
 * 패턴 분석 결과가 여기로 업데이트됨.
 */
@Entity
@Table(name = "error_patterns")
public class ErrorPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    private String errorType; // phonological | visual | letter_reversal 등

    private int occurrenceCount;

    private LocalDateTime updatedAt = LocalDateTime.now();

    protected ErrorPattern() {
    }

    public ErrorPattern(User child, String errorType, int occurrenceCount) {
        this.child = child;
        this.errorType = errorType;
        this.occurrenceCount = occurrenceCount;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public String getErrorType() {
        return errorType;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void incrementOccurrence() {
        this.occurrenceCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
