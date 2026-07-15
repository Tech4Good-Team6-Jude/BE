package com.dobak.backend.repository;

import com.dobak.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByChildIdOrderByGeneratedAtDesc(Long childId);
}
