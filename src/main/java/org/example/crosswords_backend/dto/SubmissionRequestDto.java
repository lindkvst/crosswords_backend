package org.example.crosswords_backend.dto;

import java.util.List;

// What the frontend sends: the grid as the player filled it in ('.' for an empty cell), and the attempt that was
// started when the crossword was opened. The backend computes the time from the attempt, not from the browser.
public record SubmissionRequestDto(List<String> grid, int attemptId) {
}
