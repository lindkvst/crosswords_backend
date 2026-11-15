package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.SolveAttemptDto;
import org.example.crosswords_backend.exception.NotOwnerException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.SolveAttempt;
import org.example.crosswords_backend.repository.SolveAttemptRepository;
import org.springframework.stereotype.Service;

@Service
public class SolveAttemptService {

    private final SolveAttemptRepository solveAttemptRepository;
    private final CrosswordService crosswordService;
    private final UserService userService;

    public SolveAttemptService(SolveAttemptRepository solveAttemptRepository,
                               CrosswordService crosswordService,
                               UserService userService) {
        this.solveAttemptRepository = solveAttemptRepository;
        this.crosswordService = crosswordService;
        this.userService = userService;
    }

    public SolveAttemptDto start(int crosswordId, String username) {
        Crossword crossword = crosswordService.findEntityById(crosswordId);
        AppUser user = userService.findEntityByUsername(username);
        SolveAttempt saved = solveAttemptRepository.save(new SolveAttempt(crossword, user));
        return new SolveAttemptDto(saved.getId(), crosswordId, saved.getStartedOn());
    }

    // Starting early only makes the time longer, so the player gains nothing by cheating with the start.
    // Using another player's attempt, or one for another crossword, is refused.
    public SolveAttempt findOwnAttempt(int attemptId, int crosswordId, String username) {
        SolveAttempt attempt = solveAttemptRepository.findById(attemptId)
                .filter(found -> found.getCrossword().getId() == crosswordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No attempt with id " + attemptId + " for crossword " + crosswordId));
        requireOwner(attempt, username);
        return attempt;
    }

    private void requireOwner(SolveAttempt attempt, String username) {
        if (!attempt.getUser().getUsername().equals(username)) {
            throw new NotOwnerException("You can only submit with your own attempt");
        }
    }
}
