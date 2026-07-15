package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * 게이미피케이션(알/부화). 아이 1명당 1행.
 * 학습(발음 연습 등) 진행할 때마다 자동으로 알이 쌓이고, 10개 모이면 부화 -> 리워드 지급.
 * (칭찬 도장을 보내면 이 아래 stampCount가 별도로 쌓임 — Report.stampSent 참고)
 */
@Entity
@Table(name = "progress")
public class Progress {

    public static final int EGGS_PER_HATCH = 10; // 알 한 판(부화 1회) 기준치 (임의값, 기획 확정 필요)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User child;

    private int eggCount;
    private int currentHatchProgress;
    private int totalHatchesCompleted;

    /** 부화하면 지급될 리워드 아이템 코드 (RewardCatalogService 참조) */
    private String selectedRewardCode = "honeycombo";

    /** 위 리워드를 선택해서 보낸 보호자 (보호자가 고른 적 없으면 null) */
    @ManyToOne
    private User selectedByGuardian;

    /**
     * 칭찬도장 누적 개수. 알(eggCount)이랑 완전히 별개 카운터.
     * 보호자가 리포트 화면에서 "칭찬 도장 보내기"를 누를 때마다 +1 (유치원 포도알 스티커판 느낌).
     * 부화 같은 기준치/보상 없이 그냥 계속 누적됨.
     */
    private int stampCount;

    /**
     * 발음 정확도 평균 캐싱용. 매 발음점검(PracticeService.submit) 시점마다
     * accuracySum/accuracySampleCount를 갱신해서, 리포트 등에서 매번 전체 기록을
     * 다시 스캔하지 않고 바로 평균을 읽을 수 있게 한다.
     * (참고: ReportService의 주간 리포트 정확도는 이 값과 별개로, 그 주 기간의
     *  PracticeAttempt만 따로 평균 내서 씀 — 이건 "전체 기간" 평균 캐시용)
     */
    private double accuracySum;
    private int accuracySampleCount;

    protected Progress() {
    }

    public Progress(User child) {
        this.child = child;
        this.eggCount = 0;
        this.currentHatchProgress = 0;
        this.totalHatchesCompleted = 0;
        this.stampCount = 0;
        this.accuracySum = 0;
        this.accuracySampleCount = 0;
    }

    /**
     * 학습 진행 시 자동 호출 — 알 하나 추가, 기준치 채우면 부화.
     * @return 이번 호출로 막 부화했으면 true (호출한 쪽에서 리워드 발급 트리거로 씀)
     */
    public boolean addEgg() {
        eggCount++;
        currentHatchProgress++;
        if (currentHatchProgress >= EGGS_PER_HATCH) {
            currentHatchProgress = 0;
            totalHatchesCompleted++;
            return true;
        }
        return false;
    }

    /** 보호자가 칭찬 도장을 보낼 때 호출 */
    public void addStamp() {
        stampCount++;
    }

    /** 발음점검 1회 끝날 때마다 호출 — 정확도를 곧바로 저장하지 않고 누적 평균에 반영 */
    public void recordAccuracy(double accuracy) {
        accuracySum += accuracy;
        accuracySampleCount++;
    }

    public double getAverageAccuracy() {
        return accuracySampleCount == 0 ? 0 : accuracySum / accuracySampleCount;
    }

    /** 보호자가 다음 부화 때 받을 리워드를 미리 골라둘 때 호출 */
    public void selectReward(String rewardCode, User guardian) {
        this.selectedRewardCode = rewardCode;
        this.selectedByGuardian = guardian;
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public int getEggCount() {
        return eggCount;
    }

    public int getCurrentHatchProgress() {
        return currentHatchProgress;
    }

    public int getTotalHatchesCompleted() {
        return totalHatchesCompleted;
    }

    public String getSelectedRewardCode() {
        return selectedRewardCode;
    }

    public User getSelectedByGuardian() {
        return selectedByGuardian;
    }

    public int getStampCount() {
        return stampCount;
    }

    public int getAccuracySampleCount() {
        return accuracySampleCount;
    }
}
