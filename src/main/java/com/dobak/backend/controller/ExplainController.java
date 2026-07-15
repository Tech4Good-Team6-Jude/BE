package com.dobak.backend.controller;

import com.dobak.backend.dto.ExplainResponse;
import com.dobak.backend.dto.WordCardResponse;
import com.dobak.backend.service.ExplainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ExplainController {

    private final ExplainService explainService;

    public ExplainController(ExplainService explainService) {
        this.explainService = explainService;
    }

    /**
     * level=0: 원문 낭독 + 하이라이트 (2.1)
     * level=1~3: 단계별 쉬운 문장 재설명, 숫자가 클수록 더 쉬움 (2.2)
     * 사진 캡처(1번 기능) 흐름 전용 — sentenceId는 CaptureSentence 기준.
     */
    @PostMapping("/sentences/{sentenceId}/explain")
    public ExplainResponse explain(@PathVariable Long sentenceId,
                                    @RequestParam(defaultValue = "0") int level) {
        return explainService.explain(sentenceId, level);
    }

    /**
     * 도서관(L5 반복학습 "이해" 탭) 흐름 전용 — stuckSentenceId 기준으로 같은 방식의 재설명을 내려준다.
     */
    @PostMapping("/stuck-sentences/{stuckSentenceId}/explain")
    public ExplainResponse explainStuckSentence(@PathVariable Long stuckSentenceId,
                                                 @RequestParam(defaultValue = "0") int level) {
        return explainService.explainStuckSentence(stuckSentenceId, level);
    }

    /** 문장 내 단어를 탭하면 뜻/예문/발음 카드 (2.3) */
    @GetMapping("/words/{word}/card")
    public WordCardResponse getWordCard(@PathVariable String word) {
        return explainService.getWordCard(word);
    }
}
