package org.example.crosswords_backend.service;

import org.example.crosswords_backend.client.CrosswordSolver;
import org.example.crosswords_backend.client.ClaudeSolution;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.exception.ModelNotAllowedException;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.Role;
import org.springframework.stereotype.Service;

@Service
public class ClaudeSubmissionService {

    private final CrosswordService crosswordService;
    private final UserService userService;
    private final CrosswordSolver crosswordSolver;
    private final ApiCallService apiCallService;
    private final SubmissionRecorder submissionRecorder;

    public ClaudeSubmissionService(CrosswordService crosswordService,
                                   UserService userService,
                                   CrosswordSolver crosswordSolver,
                                   ApiCallService apiCallService,
                                   SubmissionRecorder submissionRecorder) {
        this.crosswordService = crosswordService;
        this.userService = userService;
        this.crosswordSolver = crosswordSolver;
        this.apiCallService = apiCallService;
        this.submissionRecorder = submissionRecorder;
    }

    public SubmissionResultDto solve(int crosswordId, ClaudeModel model, int userId) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityById(userId);
        // Checked before the call, because the call is what costs money.
        requireModelAllowed(model, user);

        Timed<ClaudeSolution> solution = solveWithClaude(crossword, model);
        SubmissionRecorder.Recorded recorded = submissionRecorder.record(
                crossword, user, null, solution.result().grid(), model, solution.ms());
        apiCallService.recordClaudeCall(crossword, recorded.submission(), model, solution.ms(), solution.result());
        return recorded.result();
    }

    // Claude gets exactly what a player sees: the puzzle built from emptyPuzzleJson, never the answer key.
    private Timed<ClaudeSolution> solveWithClaude(Crossword crossword, ClaudeModel model) {
        PuzzleDto puzzle = crosswordService.puzzleFor(crossword);
        return Timed.measure(() -> crosswordSolver.solveCrossword(puzzle, model));
    }

    // The role comes from the database, not from the client.
    private void requireModelAllowed(ClaudeModel model, AppUser user) {
        if (model.isAdminOnly() && user.getRole() != Role.ADMIN) {
            throw new ModelNotAllowedException("Only an admin may use " + model.name());
        }
    }
}
