package com.dobak.backend.repository;

import com.dobak.backend.entity.UnknownWordQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnknownWordQueryRepository extends JpaRepository<UnknownWordQuery, Long> {
    List<UnknownWordQuery> findBySessionId(Long sessionId);
}
