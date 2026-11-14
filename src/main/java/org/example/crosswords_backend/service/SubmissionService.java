package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.CrosswordMapper;
import org.example.crosswords_backend.dto.SubmissionDto;
import org.example.crosswords_backend.dto.SubmissionMapper;
import org.example.crosswords_backend.dto.SubmissionRequestDto;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.gridlogic.GridScorer;
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

    public SubmissionService(CrosswordService crosswordService,
                             CrosswordSubmissionRepository submissionRepository,
                             CrosswordMapper crosswordMapper,
                             SubmissionMapper submissionMapper) {
        this.crosswordService = crosswordService;
        this.submissionRepository = submissionRepository;
        this.crosswordMapper = crosswordMapper;
        this.submissionMapper = submissionMapper;
    }

    public SubmissionResultDto submitByPlayer(int crosswordId, SubmissionRequestDto request) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);

        // The percentage is computed here, never in the frontend, because only the backend has the answer key.
        List<String> answerKeyGrid = crosswordMapper.toAnswerKeyGrid(crossword);
        List<String> cellMarks = GridScorer.markCells(answerKeyGrid, request.grid());
        int percentCorrect = GridScorer.percentCorrect(cellMarks);
        int timeToSolveMs = Math.max(0, request.timeToSolveMs());

        CrosswordSubmission saved = submissionRepository.save(
                submissionMapper.toEntity(crossword, request.grid(), false, timeToSolveMs, percentCorrect));
        return new SubmissionResultDto(submissionMapper.toDto(saved), cellMarks);
    }

    public List<SubmissionDto> findByCrossword(int crosswordId) {
        // Called only for its 404 when the crossword does not exist.
        crosswordService.findEntityById(crosswordId);
        return submissionRepository.findByCrosswordIdOrderByCreatedOnDesc(crosswordId).stream()
                .map(submissionMapper::toDto)
                .toList();
    }
}
