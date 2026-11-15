package org.example.crosswords_backend.service;

import org.example.crosswords_backend.client.ClaudeApiClient;
import org.example.crosswords_backend.client.ClaudeSolution;
import org.example.crosswords_backend.dto.CrosswordMapper;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.dto.SubmissionDto;
import org.example.crosswords_backend.dto.SubmissionMapper;
import org.example.crosswords_backend.dto.SubmissionRequestDto;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.gridlogic.GridScorer;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.exception.ModelNotAllowedException;
import org.example.crosswords_backend.exception.NotOwnerException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.model.SolveAttempt;
import org.example.crosswords_backend.repository.CrosswordSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    // Crosswords are fetched through CrosswordService, so only that service knows CrosswordRepository
    // and the "not found" rule lives in one place.
    private final CrosswordService crosswordService;
    private final CrosswordSubmissionRepository submissionRepository;
    private final CrosswordMapper crosswordMapper;
    private final SubmissionMapper submissionMapper;
    private final ClaudeApiClient claudeApiClient;
    private final UserService userService;
    private final ApiCallService apiCallService;
    private final SolveAttemptService solveAttemptService;

    public SubmissionService(CrosswordService crosswordService,
                             CrosswordSubmissionRepository submissionRepository,
                             CrosswordMapper crosswordMapper,
                             SubmissionMapper submissionMapper,
                             ClaudeApiClient claudeApiClient,
                             UserService userService,
                             ApiCallService apiCallService,
                             SolveAttemptService solveAttemptService) {
        this.crosswordService = crosswordService;
        this.submissionRepository = submissionRepository;
        this.crosswordMapper = crosswordMapper;
        this.submissionMapper = submissionMapper;
        this.claudeApiClient = claudeApiClient;
        this.userService = userService;
        this.apiCallService = apiCallService;
        this.solveAttemptService = solveAttemptService;
    }

    public SubmissionResultDto submitByPlayer(int crosswordId, SubmissionRequestDto request, String username) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityByUsername(username);
        // Measured in the backend from when the crossword was opened, like Claude's time.
        SolveAttempt attempt = solveAttemptService.findOwnAttempt(request.attemptId(), crosswordId, username);
        List<String> cellMarks = markCells(crossword, request.grid());
        CrosswordSubmission saved = save(crossword, user, attempt, request.grid(), null, attempt.elapsedMs(), cellMarks);
        return toResult(saved, request.grid(), cellMarks);
    }

    public SubmissionResultDto submitByClaude(int crosswordId, ClaudeModel model, String username) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityByUsername(username);
        // Checked before the call, because the call is what costs money.
        requireModelAllowed(model, user);

        Timed<ClaudeSolution> solution = solveWithClaude(crossword, model);
        List<String> grid = solution.result().grid();
        List<String> cellMarks = markCells(crossword, grid);
        CrosswordSubmission saved = save(crossword, user, null, grid, model, solution.ms(), cellMarks);
        apiCallService.recordClaudeCall(crossword, saved, model, solution.ms(), solution.result());
        return toResult(saved, grid, cellMarks);
    }

    // Shows a saved submission again, scored the same way as when it was made.
    // Only for its owner: Claude's 100 % submission is in practice the answer key.
    public SubmissionResultDto findOwnSubmission(int crosswordId, int submissionId, String username) {
        CrosswordSubmission submission = findSubmission(crosswordId, submissionId);
        requireOwner(submission, username);
        List<String> submittedGrid = submissionMapper.toSubmittedGrid(submission);
        return toResult(submission, submittedGrid, markCells(submission.getCrossword(), submittedGrid));
    }

    public List<SubmissionDto> findByCrossword(int crosswordId) {
        // Called only for its 404 when the crossword does not exist.
        crosswordService.findEntityById(crosswordId);
        return submissionRepository.findByCrosswordIdOrderByCreatedOnDesc(crosswordId).stream()
                .map(submissionMapper::toDto)
                .toList();
    }

    // A submission is only found under its own crossword, so /crosswords/1/submissions/7 cannot show a submission of crossword 2.
    private CrosswordSubmission findSubmission(int crosswordId, int submissionId) {
        return submissionRepository.findById(submissionId)
                .filter(submission -> submission.getCrossword().getId() == crosswordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No submission with id " + submissionId + " for crossword " + crosswordId));
    }

    // A submission from before V2.0 has no user, so nobody owns it.
    // A Claude submission belongs to the user who asked Claude, see decision 54.
    private void requireOwner(CrosswordSubmission submission, String username) {
        if (submission.getUser() == null || !submission.getUser().getUsername().equals(username)) {
            throw new NotOwnerException("You can only see your own submissions");
        }
    }

    // Claude gets exactly what a player sees: the puzzle built from emptyPuzzleJson by the same mapper as the frontend.
    private Timed<ClaudeSolution> solveWithClaude(Crossword crossword, ClaudeModel model) {
        PuzzleDto puzzle = crosswordMapper.toDto(crossword).puzzle();
        return Timed.measure(() -> claudeApiClient.solveCrossword(puzzle, model));
    }

    // The role comes from the database, not from the client.
    private void requireModelAllowed(ClaudeModel model, AppUser user) {
        if (model.isAdminOnly() && user.getRole() != Role.ADMIN) {
            throw new ModelNotAllowedException("Only an admin may use " + model.name());
        }
    }

    // Player and Claude are scored in exactly the same way.
    // Computed here, never in the frontend, because only the backend has the answer key.
    private List<String> markCells(Crossword crossword, List<String> submittedGrid) {
        return GridScorer.markCells(crosswordMapper.toAnswerKeyGrid(crossword), submittedGrid);
    }

    // A player has an attempt and no llmModel. Claude has an llmModel and no attempt.
    private CrosswordSubmission save(Crossword crossword, AppUser user, SolveAttempt attempt, List<String> submittedGrid,
                                     ClaudeModel llmModel, int timeToSolveMs, List<String> cellMarks) {
        int percentCorrect = GridScorer.percentCorrect(cellMarks);
        return submissionRepository.save(submissionMapper.toEntity(
                crossword, user, attempt, submittedGrid, llmModel, timeToSolveMs, percentCorrect));
    }

    private SubmissionResultDto toResult(CrosswordSubmission saved, List<String> submittedGrid, List<String> cellMarks) {
        return new SubmissionResultDto(submissionMapper.toDto(saved), submittedGrid, cellMarks);
    }
}
