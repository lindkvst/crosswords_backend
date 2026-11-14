package org.example.crosswords_backend.dto;

import java.time.LocalDateTime;

public record CrosswordDto(int id, LocalDateTime createdOn, PuzzleDto puzzle) {
}
