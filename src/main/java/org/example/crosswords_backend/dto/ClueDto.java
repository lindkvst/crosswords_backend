package org.example.crosswords_backend.dto;

// Deliberately has no answer field: this is what the frontend and Claude are allowed to see.
public record ClueDto(
        int number,
        String direction,
        int row,
        int col,
        int length,
        String text
) {
}
