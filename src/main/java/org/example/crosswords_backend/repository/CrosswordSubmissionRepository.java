package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.CrosswordSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrosswordSubmissionRepository extends JpaRepository<CrosswordSubmission, Integer> {

    List<CrosswordSubmission> findByCrosswordIdOrderByCreatedOnDesc(int crosswordId);
}
