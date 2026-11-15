package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.SolveAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolveAttemptRepository extends JpaRepository<SolveAttempt, Integer> {
}
