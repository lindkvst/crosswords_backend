package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.SubmissionDto;
import org.example.crosswords_backend.dto.SubmissionMapper;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.exception.NotOwnerException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.example.crosswords_backend.repository.CrosswordSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Reading saved submissions. Making them is PlayerSubmissionService's and ClaudeSubmissionService's job.
@Service
public class SubmissionService {

    // Crosswords are fetched through CrosswordService, so only that service knows CrosswordRepository
    // and the "not found" rule lives in one place.
    private final CrosswordService crosswordService;
    private final CrosswordSubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final SubmissionRecorder submissionRecorder;

    public SubmissionService(CrosswordService crosswordService,
                             CrosswordSubmissionRepository submissionRepository,
                             SubmissionMapper submissionMapper,
                             SubmissionRecorder submissionRecorder) {
        this.crosswordService = crosswordService;
        this.submissionRepository = submissionRepository;
        this.submissionMapper = submissionMapper;
        this.submissionRecorder = submissionRecorder;
    }

    // Shows a saved submission again, scored the same way as when it was made.
    // Only for its owner: Claude's 100 % submission is in practice the answer key.
    public SubmissionResultDto findOwnSubmission(int crosswordId, int submissionId, int userId) {
        CrosswordSubmission submission = findSubmission(crosswordId, submissionId);
        requireOwner(submission, userId);
        List<String> submittedGrid = submissionMapper.toSubmittedGrid(submission);
        List<String> cellMarks = submissionRecorder.markCells(submission.getCrossword(), submittedGrid);
        return submissionRecorder.toResult(submission, submittedGrid, cellMarks);
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
    private void requireOwner(CrosswordSubmission submission, int userId) {
        if (submission.getUser() == null || submission.getUser().getId() != userId) {
            throw new NotOwnerException("You can only see your own submissions");
        }
    }
}
