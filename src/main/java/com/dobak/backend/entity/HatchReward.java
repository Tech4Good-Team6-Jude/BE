package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 알(Progress)이 한 판(10개) 채워져서 "부화"할 때마다 발급되는 리워드 1건 (기프티콘 등).
 * 발급 시점의 카탈로그 정보(브랜드/이름/이미지/유효기간)를 스냅샷으로 저장해서,
 * 나중에 카탈로그 내용이 바뀌어도 이미 받은 리워드 기록은 안 바뀌게 한다.
 */
@Entity
@Table(name = "hatch_rewards")
public class HatchReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User child;

    private String rewardCode;
    private String brand;
    private String name;
    private String imageUrl;

    private LocalDateTime issuedAt = LocalDateTime.now();
    private LocalDateTime validUntil;

    /** 이 리워드를 선택해서 보낸 보호자 (보호자가 고른 적 없으면 null — "엄마가 보냈어요" 태그 표시용) */
    @ManyToOne
    private User sentByGuardian;

    private boolean claimed = false;
    private LocalDateTime claimedAt;

    protected HatchReward() {
    }

    public HatchReward(User child, String rewardCode, String brand, String name, String imageUrl,
                        int validDays, User sentByGuardian) {
        this.child = child;
        this.rewardCode = rewardCode;
        this.brand = brand;
        this.name = name;
        this.imageUrl = imageUrl;
        this.validUntil = this.issuedAt.plusDays(validDays);
        this.sentByGuardian = sentByGuardian;
    }

    /** "기프티콘 받기" 버튼 눌렀을 때 */
    public void claim() {
        this.claimed = true;
        this.claimedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public String getRewardCode() {
        return rewardCode;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public User getSentByGuardian() {
        return sentByGuardian;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }
}
