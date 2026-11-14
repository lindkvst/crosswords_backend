package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.ClaudeModel;

import java.time.LocalDateTime;

// The result of one submission, as shown in the list of submissions. llmModel is null for a player.
public record SubmissionDto(
        int id,
        int crosswordId,
        ClaudeModel llmModel,
        int timeToSolveMs,
        int percentCorrect,
        LocalDateTime createdOn
) {
}
