package com.dobak.backend.repository;

import com.dobak.backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByChildId(Long childId);
}
