package org.example.crosswords_backend.dto;

import java.util.List;

// The crossword without answers. In grid, '.' is an empty cell and '#' is a black cell.
public record PuzzleDto(List<String> grid, List<ClueDto> clues) {
}
