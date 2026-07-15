package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 게임 학습 - "짝 맞추기"(소리-글자 짝 맞추기, 두 장을 뒤집어 같은 짝 찾기)의 정답 원본.
 * 문장이 아니라 단어 하나 + 그 단어를 읽어주는 오디오가 한 쌍(pair)이다.
 * FE는 한 pair당 카드 2장(소리 카드/글자 카드)을 만들어서 섞은 뒤 짝맞추기 UI를 구성하고,
 * 두 카드의 pairId가 같으면 정답으로 판정한다 — 매칭 로직 자체는 전부 FE가 담당.
 *
 * audioUrl은 TTS mock이 아니라 실제로 녹음해서 업로드한 파일 경로를 저장한다
 * (GameController의 pair 생성 API로 word + 녹음파일을 같이 올리면 FileStorageService가
 * 디스크에 저장하고 그 URL을 여기 넣어줌).
 */
@Entity
@Table(name = "word_match_pairs")
public class WordMatchPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;

    private String audioUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected WordMatchPair() {
    }

    public WordMatchPair(String word, String audioUrl) {
        this.word = word;
        this.audioUrl = audioUrl;
    }

    public Long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
