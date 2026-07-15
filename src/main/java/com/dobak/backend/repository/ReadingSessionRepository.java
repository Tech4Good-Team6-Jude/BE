package com.dobak.backend.repository;

import com.dobak.backend.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByChildId(Long childId);
}
