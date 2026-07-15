package com.dobak.backend.repository;

import com.dobak.backend.entity.HatchReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HatchRewardRepository extends JpaRepository<HatchReward, Long> {
    List<HatchReward> findByChildIdOrderByIssuedAtDesc(Long childId);

    /** 부화 직후 "7·부화" 화면에 바로 보여줄 가장 최근(=방금 받은) 리워드 */
    Optional<HatchReward> findFirstByChildIdOrderByIssuedAtDesc(Long childId);
}
