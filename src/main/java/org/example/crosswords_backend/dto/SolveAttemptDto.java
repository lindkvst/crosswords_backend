package org.example.crosswords_backend.dto;

import java.time.LocalDateTime;

// The frontend keeps the id and sends it back with the submission. startedOn is only for display.
public record SolveAttemptDto(int id, int crosswordId, LocalDateTime startedOn) {
}
