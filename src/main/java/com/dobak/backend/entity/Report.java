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

    private String highlight;

    @Lob
    @Column(length = 5000)
    private String summary;

    private double accuracy;
    private double previousAccuracy;
    private int learningSessionCount;
    private int additionalSentenceCount;

    private boolean stampSent = false;

    private LocalDateTime generatedAt = LocalDateTime.now();

    protected Report() {
    }

    public Report(User child, LocalDateTime periodStart, LocalDateTime periodEnd, String highlight,
                  String summary, double accuracy, double previousAccuracy,
                  int learningSessionCount, int additionalSentenceCount) {
        this.child = child;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.highlight = highlight;
        this.summary = summary;
        this.accuracy = accuracy;
        this.previousAccuracy = previousAccuracy;
        this.learningSessionCount = learningSessionCount;
        this.additionalSentenceCount = additionalSentenceCount;
    }

    public void sendStamp() {
        this.stampSent = true;
    }

    /**
     * 같은 날 리포트를 다시 생성할 때, 새 행을 또 만들지 않고 기존 행 내용을 최신화한다.
     * stampSent는 건드리지 않는다 — 오늘 이미 도장을 보냈으면 데이터 갱신 때문에 사라지면 안 됨.
     */
    public void updateSnapshot(LocalDateTime periodStart, LocalDateTime periodEnd, String highlight, String summary,
                                double accuracy, double previousAccuracy,
                                int learningSessionCount, int additionalSentenceCount) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.highlight = highlight;
        this.summary = summary;
        this.accuracy = accuracy;
        this.previousAccuracy = previousAccuracy;
        this.learningSessionCount = learningSessionCount;
        this.additionalSentenceCount = additionalSentenceCount;
    }

    public boolean isStampSent() {
        return stampSent;
    }

    public Long getId() { return id; }
    public User getChild() { return child; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public String getHighlight() { return highlight; }
    public String getSummary() { return summary; }
    public double getAccuracy() { return accuracy; }
    public double getPreviousAccuracy() { return previousAccuracy; }
    public int getLearningSessionCount() { return learningSessionCount; }
    public int getAdditionalSentenceCount() { return additionalSentenceCount; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}
