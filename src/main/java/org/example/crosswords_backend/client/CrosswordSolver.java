package org.example.crosswords_backend.client;

import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.model.ClaudeModel;

// Something that tries to solve a crossword. Takes a PuzzleDto, which has no answers, so no implementation can be
// given the answer key. ClaudeSubmissionService depends on this interface, not on Claude: the real Claude API normally,
// a free fake during development, a mock in the tests. app.crossword-solver picks the implementation.
public interface CrosswordSolver {

    ClaudeSolution solveCrossword(PuzzleDto puzzle, ClaudeModel model);
}
