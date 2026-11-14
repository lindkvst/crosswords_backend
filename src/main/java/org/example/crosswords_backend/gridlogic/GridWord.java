package org.example.crosswords_backend.gridlogic;

// One word in a filled grid: where it starts, which way it runs, and the answer.
public record GridWord(int number, String direction, int row, int col, String word) {
}
