package org.example.crosswords_backend.dto;

import java.time.LocalDateTime;

// theme is the player's free-text prompt. texs cannot fill by theme, so it is only shown, not used for the words.
public record CrosswordDto(int id, LocalDateTime createdOn, String theme, PuzzleDto puzzle) {
}
