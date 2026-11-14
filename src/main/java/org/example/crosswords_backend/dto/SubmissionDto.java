package org.example.crosswords_backend.dto;

import java.time.LocalDateTime;

// The result of one submission, as shown in the list of submissions.
public record SubmissionDto(
        int id,
        int crosswordId,
        boolean llmAssisted,
        int timeToSolveMs,
        int percentCorrect,
        LocalDateTime createdOn
) {
}
