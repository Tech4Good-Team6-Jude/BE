package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * 게이미피케이션(포도알). 아이 1명당 1행.
 * grapeCount가 판(bunch) 기준치를 채우면 currentBunchCount가 리셋되고 totalBunchesCompleted가 증가.
 */
@Entity
@Table(name = "progress")
public class Progress {

    public static final int GRAPES_PER_BUNCH = 10; // 한 판당 포도알 개수 (임의값, 기획 확정 필요)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User child;

    private int grapeCount;

    private int currentBunchCount;

    private int totalBunchesCompleted;

    protected Progress() {
    }

    public Progress(User child) {
        this.child = child;
        this.grapeCount = 0;
        this.currentBunchCount = 0;
        this.totalBunchesCompleted = 0;
    }

    public void addGrape() {
        grapeCount++;
        currentBunchCount++;
        if (currentBunchCount >= GRAPES_PER_BUNCH) {
            currentBunchCount = 0;
            totalBunchesCompleted++;
        }
    }

    public Long getId() {
        return id;
    }

    public User getChild() {
        return child;
    }

    public int getGrapeCount() {
        return grapeCount;
    }

    public int getCurrentBunchCount() {
        return currentBunchCount;
    }

    public int getTotalBunchesCompleted() {
        return totalBunchesCompleted;
    }
}
