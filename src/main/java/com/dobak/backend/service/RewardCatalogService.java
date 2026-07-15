package com.dobak.backend.service;

import com.dobak.backend.dto.RewardCatalogItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 부화(알 10개 완성) 시 지급할 수 있는 리워드 카탈로그.
 * 해커톤 단계라 하드코딩 목록으로 관리 — Progress.selectedRewardCode 주석에서 예고했던 그 목록.
 * 실제 서비스로 갈 땐 DB 테이블 + 보호자 결제/제휴 연동으로 바뀔 영역.
 */
@Service
public class RewardCatalogService {

    private static final List<RewardCatalogItem> CATALOG = List.of(
            new RewardCatalogItem("honeycombo", "교촌치킨", "허니콤보 교환권", "/images/rewards/honeycombo.png", 30),
            new RewardCatalogItem("ppurinkle", "BHC", "뿌링클 교환권", "/images/rewards/ppurinkle.png", 30),
            new RewardCatalogItem("baskin31", "배스킨라빈스", "아이스크림 교환권(파인트)", "/images/rewards/baskin31.png", 14)
    );

    public List<RewardCatalogItem> getAll() {
        return CATALOG;
    }

    public Optional<RewardCatalogItem> findByCode(String code) {
        return CATALOG.stream().filter(item -> item.code().equals(code)).findFirst();
    }

    /** 알 수동 지급/시드 등에서 쓰는 기본값 */
    public RewardCatalogItem getDefault() {
        return CATALOG.get(0);
    }
}
