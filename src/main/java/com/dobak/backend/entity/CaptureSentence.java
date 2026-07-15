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
 * OCR로 한 장의 이미지(ReadingSession)에서 뽑아낸 문장 하나.
 * 사용자가 목록에서 이 중 하나를 골라 이해보조(explain)로 넘어간다 (1.2.1).
 * OCR 오인식 시 originalText는 그대로 두고 editedText에 수정본을 저장한다 (1.2.2).
 */
@Entity
@Table(name = "capture_sentences")
public class CaptureSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ReadingSession session;

    /** 이미지 내 문장 순서 (0부터 시작) */
    private int orderIndex;

    @Lob
    @Column(length = 2000)
    private String originalText; // OCR 원본 인식 결과, 절대 덮어쓰지 않음

    @Lob
    @Column(length = 2000)
    private String editedText; // 사용자가 직접 수정한 경우에만 채워짐 (nullable)

    private LocalDateTime createdAt = LocalDateTime.now();

    protected CaptureSentence() {
    }

    public CaptureSentence(ReadingSession session, int orderIndex, String originalText) {
        this.session = session;
        this.orderIndex = orderIndex;
        this.originalText = originalText;
    }

    /** 사용자가 OCR 오인식 문장을 직접 고쳤을 때 호출 */
    public void edit(String newText) {
        this.editedText = newText;
    }

    /** 이후 단계(이해보조 등)에서 실제로 써야 하는 최종 텍스트: 수정본이 있으면 그것, 없으면 원문 */
    public String getEffectiveText() {
        return editedText != null ? editedText : originalText;
    }

    public Long getId() {
        return id;
    }

    public ReadingSession getSession() {
        return session;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getEditedText() {
        return editedText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
