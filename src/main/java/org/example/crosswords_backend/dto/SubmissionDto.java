package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.ClaudeModel;

import java.time.LocalDateTime;

// The result of one submission, as shown in the list of submissions. llmModel is null for a player.
// username is who submitted, or who asked Claude. Only the name, never the user entity with its hash.
public record SubmissionDto(
        int id,
        int crosswordId,
        String username,
        ClaudeModel llmModel,
        int timeToSolveMs,
        int percentCorrect,
        // Watts x time for the brain (player) or the model (Claude). An estimate, see EnergyEstimator.
        double estimatedWh,
        LocalDateTime createdOn
) {
}
