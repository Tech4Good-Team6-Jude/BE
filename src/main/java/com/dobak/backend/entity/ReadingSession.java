package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 캡처(촬영) 세션 단위 — 기능명세서 1번(텍스트 캡처 및 문장 추출) 이후 흐름 전체를 묶는 anchor.
 * 사진 한 장 = ReadingSession 하나. 그 안에 여러 문장(CaptureSentence)이 딸려 있고,
 * 이후 이해보조(explain)/유사문장/발음점검도 전부 이 session을 참조해서
 * 하나의 학습 흐름으로 리포트에 묶일 수 있게 한다.
 */
@Entity
@Table(name = "reading_sessions")
public class ReadingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    /** 촬영/업로드한 원본 이미지 경로 (원본 이미지 비교 화면 1.2.2용) */
    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected ReadingSession() {
    }

    public ReadingSession(User child, String imageUrl) {
        this.child = child;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
