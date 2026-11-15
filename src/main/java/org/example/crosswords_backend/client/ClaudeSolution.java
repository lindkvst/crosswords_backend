package org.example.crosswords_backend.client;

import java.util.List;

// Claude's grid plus the token counts, so the caller can record what the call cost.
// Tokens are null if Anthropic sent no usage.
public record ClaudeSolution(List<String> grid, Integer inputTokens, Integer outputTokens) {
}
