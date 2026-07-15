package com.dobak.backend.repository;

import com.dobak.backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findAllByOrderByIdAsc();

    Optional<Book> findByTitle(String title);
}
