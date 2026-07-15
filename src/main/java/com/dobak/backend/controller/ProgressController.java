package com.dobak.backend.controller;

import com.dobak.backend.dto.ProgressResponse;
import com.dobak.backend.dto.RewardSelectionRequest;
import com.dobak.backend.service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/children/{childId}/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /** 알 현황 조회 */
    @GetMapping
    public ProgressResponse getProgress(@PathVariable Long childId) {
        return progressService.getProgress(childId);
    }

    /** 보호자가 다음 부화 때 지급될 리워드를 미리 선택 (Progress.selectReward 연결) */
    @PostMapping("/reward-selection")
    public ProgressResponse selectReward(@PathVariable Long childId, @RequestBody RewardSelectionRequest request) {
        return progressService.selectReward(childId, request.rewardCode(), request.guardianId());
    }
}
