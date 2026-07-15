package com.dobak.backend.config;

import com.dobak.backend.repository.WordMatchPairRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 짝 맞추기(소리-글자) 게임용 단어 뱅크.
 * 예전엔 여기서 가짜 오디오 경로로 12개 단어를 자동 시드했는데, 이제 오디오는 실제로 녹음한
 * 파일을 올려서 만들기로 해서(GameController의 POST /games/word-match/pairs) 자동 시드는 껐다.
 * 12개 단어를 각각 녹음해서 그 API로 하나씩 등록해주면 됨 — word-match_pairs 테이블이
 * 비어있으면 GET /games/word-match/round는 그냥 빈 배열을 돌려준다.
 */
@Component
@Order(4)
public class WordMatchPairSeeder implements CommandLineRunner {

    private final WordMatchPairRepository wordMatchPairRepository;

    public WordMatchPairSeeder(WordMatchPairRepository wordMatchPairRepository) {
        this.wordMatchPairRepository = wordMatchPairRepository;
    }

    @Override
    public void run(String... args) {
        // 의도적으로 아무것도 안 함 — 실제 녹음 파일 업로드로만 채워짐.
    }
}
