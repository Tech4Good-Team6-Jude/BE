package com.dobak.backend.repository;

import com.dobak.backend.entity.SimilarSentenceSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimilarSentenceSetRepository extends JpaRepository<SimilarSentenceSet, Long> {
}
