package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.Crossword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CrosswordRepository extends JpaRepository<Crossword, Integer> {

    // Spring Data builds the query from the name: count crosswords whose user.id matches, created after "since".
    long countByUserIdAndCreatedOnAfter(int userId, LocalDateTime since);
}
