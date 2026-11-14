package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class SubmissionMapper {

    private final ObjectMapper objectMapper;

    public SubmissionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CrosswordSubmission toEntity(Crossword crossword, List<String> submittedGrid,
                                        boolean llmAssisted, int timeToSolveMs, int percentCorrect) {
        // No user yet: users arrive with Spring Security in V2.0.
        return new CrosswordSubmission(crossword, null, objectMapper.writeValueAsString(submittedGrid),
                llmAssisted, timeToSolveMs, percentCorrect);
    }

    public SubmissionDto toDto(CrosswordSubmission submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getCrossword().getId(),
                submission.isLlmAssisted(),
                submission.getTimeToSolveMs(),
                submission.getPercentCorrect(),
                submission.getCreatedOn());
    }
}
