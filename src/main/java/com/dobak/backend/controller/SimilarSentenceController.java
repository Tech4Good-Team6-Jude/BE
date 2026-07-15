package com.dobak.backend.controller;

import com.dobak.backend.dto.SimilarSetResponse;
import com.dobak.backend.service.SimilarSentenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SimilarSentenceController {

    private final SimilarSentenceService similarSentenceService;

    public SimilarSentenceController(SimilarSentenceService similarSentenceService) {
        this.similarSentenceService = similarSentenceService;
    }

    /** 막힌 문장 기반 유사문장 세트 생성 (개수 3~5, 난이도 쉬움/보통 옵션) — 사진 캡처(1번 기능) 흐름 */
    @PostMapping("/sentences/{sentenceId}/similar-sets")
    public SimilarSetResponse createSet(@PathVariable Long sentenceId,
                                         @RequestParam(required = false) Integer count,
                                         @RequestParam(required = false) String difficulty) {
        return similarSentenceService.createSet(sentenceId, count, difficulty);
    }

    /** 막힌 문장 기반 유사문장 세트 생성 — 도서관(책 읽기) 흐름 (L5 반복학습) */
    @PostMapping("/stuck-sentences/{stuckSentenceId}/similar-sets")
    public SimilarSetResponse createSetForStuckSentence(@PathVariable Long stuckSentenceId,
                                                          @RequestParam(required = false) Integer count,
                                                          @RequestParam(required = false) String difficulty) {
        return similarSentenceService.createSetForStuckSentence(stuckSentenceId, count, difficulty);
    }

    /** 생성된 세트 재조회 (진행 상태 복원용) */
    @GetMapping("/similar-sets/{setId}")
    public SimilarSetResponse getSet(@PathVariable Long setId) {
        return similarSentenceService.getSet(setId);
    }

    /** 문장별 학습 완료 체크 */
    @PatchMapping("/similar-sets/{setId}/items/{itemId}/complete")
    public SimilarSetResponse completeItem(@PathVariable Long setId, @PathVariable Long itemId) {
        return similarSentenceService.completeItem(setId, itemId);
    }
}
