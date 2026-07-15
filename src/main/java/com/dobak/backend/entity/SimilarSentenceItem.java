package com.dobak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 유사문장 세트(SimilarSentenceSet) 안의 문장 하나. 따라읽기용 TTS 오디오를
 * 세트 생성 시점에 미리 만들어 저장해두고, 완료 여부(completed)로 진행률을 추적한다.
 */
@Entity
@Table(name = "similar_sentence_items")
public class SimilarSentenceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SimilarSentenceSet set;

    private int orderIndex;

    @Lob
    @Column(length = 1000)
    private String text;

    private String audioUrl;

    private boolean completed = false;

    protected SimilarSentenceItem() {
    }

    public SimilarSentenceItem(SimilarSentenceSet set, int orderIndex, String text, String audioUrl) {
        this.set = set;
        this.orderIndex = orderIndex;
        this.text = text;
        this.audioUrl = audioUrl;
    }

    public void complete() {
        this.completed = true;
    }

    public Long getId() {
        return id;
    }

    public SimilarSentenceSet getSet() {
        return set;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getText() {
        return text;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public boolean isCompleted() {
        return completed;
    }
}
