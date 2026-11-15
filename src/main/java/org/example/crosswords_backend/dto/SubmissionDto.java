package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.ClaudeModel;

import java.time.LocalDateTime;

// The result of one submission, as shown in the list of submissions. llmModel is null for a player.
// userId and username are who submitted, or who asked Claude. Only these two, never the user entity with its hash.
// The frontend compares userId, because two players may have the same username.
public record SubmissionDto(
        int id,
        int crosswordId,
        Integer userId,
        String username,
        ClaudeModel llmModel,
        int timeToSolveMs,
        int percentCorrect,
        // Watts x time for the brain (player) or the model (Claude). An estimate, see EnergyEstimator.
        double estimatedWh,
        LocalDateTime createdOn
) {
}
