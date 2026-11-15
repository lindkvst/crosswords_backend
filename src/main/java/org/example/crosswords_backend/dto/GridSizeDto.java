package org.example.crosswords_backend.dto;

// A size the player can choose, and how many layouts the backend picks between for that size.
public record GridSizeDto(int size, int layouts) {
}
