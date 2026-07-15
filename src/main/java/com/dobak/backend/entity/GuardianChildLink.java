package com.dobak.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 보호자 1명이 아이 여러 명을 볼 수 있는 구조 대비 (다대다 관계를 이 테이블로 표현).
 */
@Entity
@Table(name = "guardian_child_links")
public class GuardianChildLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User guardian;

    @ManyToOne
    private User child;

    protected GuardianChildLink() {
    }

    public GuardianChildLink(User guardian, User child) {
        this.guardian = guardian;
        this.child = child;
    }

    public Long getId() {
        return id;
    }

    public User getGuardian() {
        return guardian;
    }

    public User getChild() {
        return child;
    }
}
