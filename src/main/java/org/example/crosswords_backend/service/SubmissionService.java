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
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
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

    public SubmissionService(CrosswordService crosswordService,
                             CrosswordSubmissionRepository submissionRepository,
                             CrosswordMapper crosswordMapper,
                             SubmissionMapper submissionMapper,
                             ClaudeApiClient claudeApiClient,
                             UserService userService,
                             ApiCallService apiCallService) {
        this.crosswordService = crosswordService;
        this.submissionRepository = submissionRepository;
        this.crosswordMapper = crosswordMapper;
        this.submissionMapper = submissionMapper;
        this.claudeApiClient = claudeApiClient;
        this.userService = userService;
        this.apiCallService = apiCallService;
    }

    public SubmissionResultDto submitByPlayer(int crosswordId, SubmissionRequestDto request, String username) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityByUsername(username);
        List<String> cellMarks = markCells(crossword, request.grid());
        CrosswordSubmission saved = save(crossword, user, request.grid(), null,
                Math.max(0, request.timeToSolveMs()), cellMarks);
        return toResult(saved, request.grid(), cellMarks);
    }

    public SubmissionResultDto submitByClaude(int crosswordId, ClaudeModel model, String username) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityByUsername(username);

        // Built from emptyPuzzleJson by the same mapper as the frontend uses, so Claude gets exactly what a player sees.
        PuzzleDto puzzle = crosswordMapper.toDto(crossword).puzzle();

        // Here the backend measures the time itself, so it can be trusted (unlike the player's time).
        long start = System.currentTimeMillis();
        ClaudeSolution solution = claudeApiClient.solveCrossword(puzzle, model);
        int timeToSolveMs = (int) (System.currentTimeMillis() - start);

        List<String> cellMarks = markCells(crossword, solution.grid());
        CrosswordSubmission saved = save(crossword, user, solution.grid(), model, timeToSolveMs, cellMarks);
        apiCallService.recordClaudeCall(crossword, saved, model, timeToSolveMs, solution);
        return toResult(saved, solution.grid(), cellMarks);
    }

    public List<SubmissionDto> findByCrossword(int crosswordId) {
        // Called only for its 404 when the crossword does not exist.
        crosswordService.findEntityById(crosswordId);
        return submissionRepository.findByCrosswordIdOrderByCreatedOnDesc(crosswordId).stream()
                .map(submissionMapper::toDto)
                .toList();
    }

    // Player and Claude are scored in exactly the same way.
    // Computed here, never in the frontend, because only the backend has the answer key.
    private List<String> markCells(Crossword crossword, List<String> submittedGrid) {
        return GridScorer.markCells(crosswordMapper.toAnswerKeyGrid(crossword), submittedGrid);
    }

    // llmModel is null for a player.
    private CrosswordSubmission save(Crossword crossword, AppUser user, List<String> submittedGrid,
                                     ClaudeModel llmModel, int timeToSolveMs, List<String> cellMarks) {
        int percentCorrect = GridScorer.percentCorrect(cellMarks);
        return submissionRepository.save(
                submissionMapper.toEntity(crossword, user, submittedGrid, llmModel, timeToSolveMs, percentCorrect));
    }

    private SubmissionResultDto toResult(CrosswordSubmission saved, List<String> submittedGrid, List<String> cellMarks) {
        return new SubmissionResultDto(submissionMapper.toDto(saved), submittedGrid, cellMarks);
    }
}
