package com.dobak.backend.repository;

import com.dobak.backend.entity.WordMatchPair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordMatchPairRepository extends JpaRepository<WordMatchPair, Long> {
}
