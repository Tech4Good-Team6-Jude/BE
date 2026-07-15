package com.dobak.backend.service;

import com.dobak.backend.dto.WordMatchPairSummary;
import com.dobak.backend.entity.WordMatchPair;
import com.dobak.backend.repository.WordMatchPairRepository;
import com.dobak.backend.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 게임 학습 - "짝 맞추기"(소리-글자 짝 맞추기). 정답 쌍 원본(WordMatchPair) 중 한 라운드 분량을
 * 랜덤으로 뽑아 pairId가 매핑된 채로 내려준다. 섞기/뒤집기/정답 판정 등 실제 게임 로직은 FE가 담당하고,
 * 서버는 "이 라운드에 낼 정답 목록"만 책임진다.
 * 소리 카드용 오디오는 TTS mock이 아니라 직접 녹음해서 업로드한 실제 파일을 쓴다(createPair).
 */
@Service
public class WordMatchGameService {

    /** 한 라운드에 낼 쌍 개수 — 와이어프레임 기준 4쌍(8장) 고정 */
    private static final int ROUND_SIZE = 4;

    private final WordMatchPairRepository wordMatchPairRepository;
    private final FileStorageService fileStorageService;

    public WordMatchGameService(WordMatchPairRepository wordMatchPairRepository, FileStorageService fileStorageService) {
        this.wordMatchPairRepository = wordMatchPairRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<WordMatchPairSummary> getRound() {
        List<WordMatchPair> all = new ArrayList<>(wordMatchPairRepository.findAll());
        if (all.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(all);
        int size = Math.min(ROUND_SIZE, all.size());
        return all.subList(0, size).stream()
                .map(p -> new WordMatchPairSummary(p.getId(), p.getWord(), p.getAudioUrl()))
                .toList();
    }

    /** 단어 + 실제 녹음 파일을 올려서 정답 쌍 하나를 새로 만든다 */
    public WordMatchPairSummary createPair(String word, MultipartFile audio) {
        String audioUrl = fileStorageService.save(audio, "word-match-audio");
        WordMatchPair pair = new WordMatchPair(word, audioUrl);
        wordMatchPairRepository.save(pair);
        return new WordMatchPairSummary(pair.getId(), pair.getWord(), pair.getAudioUrl());
    }

    /** 전체 정답 쌍 목록 (관리용 — 뭐가 이미 올라가 있는지 확인) */
    public List<WordMatchPairSummary> listAll() {
        return wordMatchPairRepository.findAll().stream()
                .map(p -> new WordMatchPairSummary(p.getId(), p.getWord(), p.getAudioUrl()))
                .toList();
    }
}
