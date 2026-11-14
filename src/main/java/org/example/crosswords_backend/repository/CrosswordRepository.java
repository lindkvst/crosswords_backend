package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.Crossword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrosswordRepository extends JpaRepository<Crossword, Integer> {
}
