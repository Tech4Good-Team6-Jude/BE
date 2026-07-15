package com.dobak.backend.repository;

import com.dobak.backend.entity.GuardianChildLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardianChildLinkRepository extends JpaRepository<GuardianChildLink, Long> {
    List<GuardianChildLink> findByGuardianId(Long guardianId);
}
