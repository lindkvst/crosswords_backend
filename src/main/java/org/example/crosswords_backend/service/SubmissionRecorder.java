package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.SubmissionMapper;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.gridlogic.GridScorer;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.example.crosswords_backend.model.SolveAttempt;
import org.example.crosswords_backend.repository.CrosswordSubmissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

// The steps a player's and Claude's submission share: score against the answer key, save, build the answer.
// Player and Claude are scored in exactly the same way, and only here, because only the backend has the answer key.
@Component
public class SubmissionRecorder {

    private final CrosswordService crosswordService;
    private final CrosswordSubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;

    public SubmissionRecorder(CrosswordService crosswordService,
                              CrosswordSubmissionRepository submissionRepository,
                              SubmissionMapper submissionMapper) {
        this.crosswordService = crosswordService;
        this.submissionRepository = submissionRepository;
        this.submissionMapper = submissionMapper;
    }

    // A player has an attempt and no llmModel. Claude has an llmModel and no attempt.
    public Recorded record(Crossword crossword, AppUser user, SolveAttempt attempt, List<String> submittedGrid,
                           ClaudeModel llmModel, int timeToSolveMs) {
        List<String> cellMarks = markCells(crossword, submittedGrid);
        int percentCorrect = GridScorer.percentCorrect(cellMarks);
        CrosswordSubmission saved = submissionRepository.save(submissionMapper.toEntity(
                crossword, user, attempt, submittedGrid, llmModel, timeToSolveMs, percentCorrect));
        return new Recorded(saved, toResult(saved, submittedGrid, cellMarks));
    }

    public List<String> markCells(Crossword crossword, List<String> submittedGrid) {
        return GridScorer.markCells(crosswordService.answerKeyFor(crossword), submittedGrid);
    }

    public SubmissionResultDto toResult(CrosswordSubmission saved, List<String> submittedGrid, List<String> cellMarks) {
        return new SubmissionResultDto(submissionMapper.toDto(saved), submittedGrid, cellMarks);
    }

    // The saved entity is needed by Claude's service to link the API usage row; the result goes to the client.
    public record Recorded(CrosswordSubmission submission, SubmissionResultDto result) {
    }
}
