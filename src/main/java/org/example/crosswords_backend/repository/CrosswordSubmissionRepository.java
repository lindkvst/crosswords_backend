package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.CrosswordSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CrosswordSubmissionRepository extends JpaRepository<CrosswordSubmission, Integer> {

    List<CrosswordSubmission> findByCrosswordIdOrderByCreatedOnDesc(int crosswordId);

    // Claude submissions (llmModel is set) the user asked for since the given time.
    long countByUserIdAndLlmModelIsNotNullAndCreatedOnAfter(int userId, LocalDateTime since);
}
