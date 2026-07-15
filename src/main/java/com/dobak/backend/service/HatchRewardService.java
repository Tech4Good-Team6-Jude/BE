package com.dobak.backend.service;

import com.dobak.backend.dto.HatchRewardSummary;
import com.dobak.backend.dto.RewardCatalogItem;
import com.dobak.backend.entity.HatchReward;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.HatchRewardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 부화(알 10개 완성) 시 실제로 리워드를 "발급"하고, 목록 조회/수령 처리를 담당.
 * 발급 자체는 ProgressService.addEgg()에서 부화를 감지했을 때 호출한다.
 */
@Service
public class HatchRewardService {

    private final HatchRewardRepository hatchRewardRepository;
    private final RewardCatalogService rewardCatalogService;

    public HatchRewardService(HatchRewardRepository hatchRewardRepository, RewardCatalogService rewardCatalogService) {
        this.hatchRewardRepository = hatchRewardRepository;
        this.rewardCatalogService = rewardCatalogService;
    }

    /** 부화 순간 호출 — 카탈로그에서 골라둔 리워드 정보를 스냅샷으로 떠서 발급 */
    public HatchRewardSummary issue(User child, String rewardCode, User sentByGuardian) {
        RewardCatalogItem item = rewardCatalogService.findByCode(rewardCode)
                .orElseGet(rewardCatalogService::getDefault);
        HatchReward reward = new HatchReward(
                child, item.code(), item.brand(), item.name(), item.imageUrl(), item.validDays(), sentByGuardian
        );
        hatchRewardRepository.save(reward);
        return toSummary(reward);
    }

    /** "리워드 관리" 화면 — 이 아이가 지금까지 받은 리워드 전체 목록 */
    public List<HatchRewardSummary> listByChild(Long childId) {
        return hatchRewardRepository.findByChildIdOrderByIssuedAtDesc(childId).stream()
                .map(this::toSummary)
                .toList();
    }

    /** "기프티콘 받기" 버튼 */
    public HatchRewardSummary claim(Long hatchRewardId) {
        HatchReward reward = hatchRewardRepository.findById(hatchRewardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리워드: " + hatchRewardId));
        reward.claim();
        hatchRewardRepository.save(reward);
        return toSummary(reward);
    }

    private HatchRewardSummary toSummary(HatchReward r) {
        return new HatchRewardSummary(
                r.getId(), r.getRewardCode(), r.getBrand(), r.getName(), r.getImageUrl(),
                r.getIssuedAt(), r.getValidUntil(), r.getSentByGuardian() != null, r.isClaimed(), r.getClaimedAt()
        );
    }
}
