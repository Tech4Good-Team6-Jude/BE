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
 * 보호자용 리포트. PracticeAttempt/ErrorPattern을 기간 단위로 집계.
 */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    @Lob
    @Column(length = 5000)
    private String summary;

    private LocalDateTime generatedAt = LocalDateTime.now();

    protected Report() {
    }

    public Report(User child, LocalDateTime periodStart, LocalDateTime periodEnd, String summary) {
        this.child = child;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
