package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.example.crosswords_backend.service.EnergyEstimator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class SubmissionMapper {

    private final ObjectMapper objectMapper;

    public SubmissionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CrosswordSubmission toEntity(Crossword crossword, AppUser user, List<String> submittedGrid,
                                        ClaudeModel llmModel, int timeToSolveMs, int percentCorrect) {
        return new CrosswordSubmission(crossword, user, objectMapper.writeValueAsString(submittedGrid),
                llmModel, timeToSolveMs, percentCorrect);
    }

    // Computed from the stored time, so it needs no lookup. For Claude it matches the ApiCall row,
    // because the measured duration of the call is the submission's time.
    private double estimatedWh(CrosswordSubmission submission) {
        return submission.getLlmModel() == null
                ? EnergyEstimator.humanWh(submission.getTimeToSolveMs())
                : EnergyEstimator.claudeWh(submission.getLlmModel(), submission.getTimeToSolveMs());
    }

    public SubmissionDto toDto(CrosswordSubmission submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getCrossword().getId(),
                // Submissions from before V2.0 have no user.
                submission.getUser() == null ? null : submission.getUser().getUsername(),
                submission.getLlmModel(),
                submission.getTimeToSolveMs(),
                submission.getPercentCorrect(),
                estimatedWh(submission),
                submission.getCreatedOn());
    }
}
