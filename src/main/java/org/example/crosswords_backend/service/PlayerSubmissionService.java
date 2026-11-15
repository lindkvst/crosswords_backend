package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.SubmissionRequestDto;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.SolveAttempt;
import org.springframework.stereotype.Service;

@Service
public class PlayerSubmissionService {

    private final CrosswordService crosswordService;
    private final UserService userService;
    private final SolveAttemptService solveAttemptService;
    private final SubmissionRecorder submissionRecorder;

    public PlayerSubmissionService(CrosswordService crosswordService,
                                   UserService userService,
                                   SolveAttemptService solveAttemptService,
                                   SubmissionRecorder submissionRecorder) {
        this.crosswordService = crosswordService;
        this.userService = userService;
        this.solveAttemptService = solveAttemptService;
        this.submissionRecorder = submissionRecorder;
    }

    public SubmissionResultDto submit(int crosswordId, SubmissionRequestDto request, int userId) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityById(userId);
        // Measured in the backend from when the crossword was opened, like Claude's time.
        SolveAttempt attempt = solveAttemptService.findOwnAttempt(request.attemptId(), crosswordId, userId);
        return submissionRecorder.record(crossword, user, attempt, request.grid(), null, attempt.elapsedMs()).result();
    }
}
