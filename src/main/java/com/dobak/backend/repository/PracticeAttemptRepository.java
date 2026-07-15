package com.dobak.backend.repository;

import com.dobak.backend.entity.PracticeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, Long> {
    List<PracticeAttempt> findByChildIdOrderByCreatedAtDesc(Long childId);
    List<PracticeAttempt> findByChildIdAndCreatedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
}
