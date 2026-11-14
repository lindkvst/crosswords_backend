package org.example.crosswords_backend.dto;

import java.util.List;

// What the frontend sends: the grid as the player filled it in ('.' for an empty cell),
// and how long it took.
public record SubmissionRequestDto(List<String> grid, int timeToSolveMs) {
}
