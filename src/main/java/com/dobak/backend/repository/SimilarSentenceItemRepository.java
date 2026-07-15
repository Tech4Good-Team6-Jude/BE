package com.dobak.backend.repository;

import com.dobak.backend.entity.SimilarSentenceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimilarSentenceItemRepository extends JpaRepository<SimilarSentenceItem, Long> {
    List<SimilarSentenceItem> findBySetIdOrderByOrderIndexAsc(Long setId);
}
