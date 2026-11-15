package org.example.crosswords_backend.dto;

// Only what the dropdown needs. The layout itself is not secret, but the frontend has no use for it.
public record GridTemplateDto(int id, String name, int rows, int cols) {
}
