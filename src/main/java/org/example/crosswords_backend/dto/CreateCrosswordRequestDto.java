package org.example.crosswords_backend.dto;

// What the frontend sends to create a crossword: which grid size, and an optional theme.
public record CreateCrosswordRequestDto(int gridTemplateId, String playerPrompt) {
}
