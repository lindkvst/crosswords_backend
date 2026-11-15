package org.example.crosswords_backend.client;

import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.model.ClaudeModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

// Development only (app.crossword-solver=fake): "solves" without calling Anthropic, so the Claude button can be tried
// for free. It only sees the empty puzzle, like Claude, and writes 'A' in every white cell, so the score is low.
@Component
@ConditionalOnProperty(name = "app.crossword-solver", havingValue = "fake")
public class FakeCrosswordSolver implements CrosswordSolver {

    @Override
    public ClaudeSolution solveCrossword(PuzzleDto puzzle, ClaudeModel model) {
        List<String> grid = puzzle.grid().stream().map(row -> row.replace('.', 'A')).toList();
        return new ClaudeSolution(grid, 0, 0);
    }
}
