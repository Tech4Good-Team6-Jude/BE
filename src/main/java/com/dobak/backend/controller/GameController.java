package com.dobak.backend.controller;

import com.dobak.backend.dto.WordMatchPairSummary;
import com.dobak.backend.service.WordMatchGameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 게임 학습 (Frame 2). 현재는 짝 맞추기(소리-글자)만 구현. */
@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final WordMatchGameService wordMatchGameService;

    public GameController(WordMatchGameService wordMatchGameService) {
        this.wordMatchGameService = wordMatchGameService;
    }

    /**
     * 짝 맞추기 라운드 조회. 정답 쌍 4개를 pairId와 함께 그대로 내려주며,
     * 카드 섞기/뒤집기/정답 판정은 FE에서 처리한다(같은 pairId 카드 2장을 찾으면 성공).
     */
    @GetMapping("/word-match/round")
    public List<WordMatchPairSummary> getRound() {
        return wordMatchGameService.getRound();
    }

    /**
     * 단어 + 실제 녹음 파일을 올려서 정답 쌍을 하나 등록 (콘텐츠 준비용, 관리자/기획자가 미리 채워넣는 API).
     * 12개 단어를 각각 녹음해서 이 API로 하나씩 올리면 됨.
     */
    @PostMapping(value = "/word-match/pairs", consumes = "multipart/form-data")
    public WordMatchPairSummary createPair(@RequestParam("word") String word,
                                            @RequestParam("audio") MultipartFile audio) {
        return wordMatchGameService.createPair(word, audio);
    }

    /** 지금까지 등록된 정답 쌍 전체 목록 (뭐가 이미 올라가 있는지 확인용) */
    @GetMapping("/word-match/pairs")
    public List<WordMatchPairSummary> listPairs() {
        return wordMatchGameService.listAll();
    }
}
