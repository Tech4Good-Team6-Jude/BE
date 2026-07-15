package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * B모드: 발음 연습/점검 1회 기록. 실제 녹음 파일을 저장해서 이전 시도와 비교 가능.
 * STT/발음평가는 Inference Server가 담당, 여기엔 결과만 저장.
 */
@Entity
@Table(name = "practice_attempts")
public class PracticeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    private String targetText;

    private String audioFileUrl;

    private String sttResult;

    private double accuracy;

    /** 이 목표 문장이 속한 소리 패턴 (예: 겹받침/된소리/긴문장). 리포트의 소리연습 집계에 쓰임. */
    private String pattern;

    private Long comparedToAttemptId; // 이전 시도와 비교할 때 참조 (nullable)

    private LocalDateTime createdAt = LocalDateTime.now();

    protected PracticeAttempt() {
    }

    public PracticeAttempt(User child, String targetText, String audioFileUrl,
                            String sttResult, double accuracy, String pattern, Long comparedToAttemptId) {
        this.child = child;
        this.targetText = targetText;
        this.audioFileUrl = audioFileUrl;
        this.sttResult = sttResult;
        this.accuracy = accuracy;
        this.pattern = pattern;
        this.comparedToAttemptId = comparedToAttemptId;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public String getTargetText() {
        return targetText;
    }

    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public String getSttResult() {
        return sttResult;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getPattern() {
        return pattern;
    }

    public Long getComparedToAttemptId() {
        return comparedToAttemptId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
