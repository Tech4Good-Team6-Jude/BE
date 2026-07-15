package com.dobak.backend.controller;

import com.dobak.backend.dto.HatchRewardSummary;
import com.dobak.backend.dto.RewardCatalogItem;
import com.dobak.backend.service.HatchRewardService;
import com.dobak.backend.service.RewardCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** "6·리워드"/"7·부화"/보호자 리포트의 "리워드 관리" 화면용 */
@RestController
@RequestMapping("/api/v1")
public class RewardController {

    private final RewardCatalogService rewardCatalogService;
    private final HatchRewardService hatchRewardService;

    public RewardController(RewardCatalogService rewardCatalogService, HatchRewardService hatchRewardService) {
        this.rewardCatalogService = rewardCatalogService;
        this.hatchRewardService = hatchRewardService;
    }

    /** 보호자가 리워드를 고를 때 보여줄 전체 목록 */
    @GetMapping("/rewards/catalog")
    public List<RewardCatalogItem> getCatalog() {
        return rewardCatalogService.getAll();
    }

    /** 이 아이가 지금까지 부화로 받은 리워드 전체 이력 (최신순) */
    @GetMapping("/children/{childId}/hatch-rewards")
    public List<HatchRewardSummary> listHatchRewards(@PathVariable Long childId) {
        return hatchRewardService.listByChild(childId);
    }

    /** "기프티콘 받기" 버튼 */
    @PostMapping("/hatch-rewards/{hatchRewardId}/claim")
    public HatchRewardSummary claim(@PathVariable Long hatchRewardId) {
        return hatchRewardService.claim(hatchRewardId);
    }
}
