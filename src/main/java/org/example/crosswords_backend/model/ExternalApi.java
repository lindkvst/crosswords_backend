package org.example.crosswords_backend.model;

// The external calls we record usage for. One crossword costs a fill and a clue lookup at texs.
public enum ExternalApi {
    TEXS_FILL,
    TEXS_CLUES,
    CLAUDE_SOLVE
}
