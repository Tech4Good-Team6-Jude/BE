package com.dobak.backend.repository;

import com.dobak.backend.entity.ErrorPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ErrorPatternRepository extends JpaRepository<ErrorPattern, Long> {
    List<ErrorPattern> findByChildId(Long childId);
    Optional<ErrorPattern> findByChildIdAndErrorType(Long childId, String errorType);
}
