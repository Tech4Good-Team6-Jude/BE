package com.dobak.backend.repository;

import com.dobak.backend.entity.CaptureSentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaptureSentenceRepository extends JpaRepository<CaptureSentence, Long> {
    List<CaptureSentence> findBySessionIdOrderByOrderIndexAsc(Long sessionId);

    /** 리포트의 "읽은 문장" 카운트용 — 촬영/도서관 어느 경로든 세션에 딸린 문장 수를 기간별로 센다. */
    long countBySession_ChildIdAndSession_CreatedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
}
