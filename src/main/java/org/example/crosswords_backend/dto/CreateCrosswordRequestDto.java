package org.example.crosswords_backend.dto;

// What the frontend sends to create a crossword: the grid size (5 for 5x5) and an optional theme.
// The backend picks the layout, so the black cells vary between crosswords of the same size.
public record CreateCrosswordRequestDto(int gridSize, String playerPrompt) {
}
