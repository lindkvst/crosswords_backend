package org.example.crosswords_backend.client;

import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.exception.ClaudeAnswerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class ClaudeApiClient {

    // Room for Claude's thinking as well as the short answer. Thinking counts towards this limit.
    private static final int MAX_TOKENS = 8000;
    // WebClient waits forever by default. A stuck call should fail instead of hanging the request.
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private static final String SYSTEM_PROMPT = """
            You solve Danish crosswords.
            You get the empty grid and the clues as JSON.
            In the grid, '.' is a cell to fill in and '#' is a black cell. Rows and columns count from 0.
            Each clue gives the start cell (row, col), the direction (across or down), the length and the clue text.
            The clues are dictionary definitions, and a word can have an uncommon meaning.
            Answer with the full grid: the same number of rows and the same row length,
            '#' unchanged, and every '.' replaced by one uppercase letter (Æ, Ø and Å are allowed).
            Crossing words share their letters, so use them to check each other.
            If you are unsure of a cell, keep '.'.
            """;

    // The JSON schema Claude's answer must follow: {"grid": ["ROW1", "ROW2", ...]}
    private static final Map<String, Object> GRID_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of("grid", Map.of("type", "array", "items", Map.of("type", "string"))),
            "required", List.of("grid"),
            "additionalProperties", false);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public ClaudeApiClient(@Qualifier("claudeWebClient") WebClient webClient,
                           ObjectMapper objectMapper,
                           @Value("${anthropic.model}") String model) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    // Takes a PuzzleDto, which has no answers. That type is the guarantee that Claude never sees the answer key.
    public List<String> solveCrossword(PuzzleDto puzzle) {
        ClaudeRequest request = new ClaudeRequest(
                model,
                MAX_TOKENS,
                SYSTEM_PROMPT,
                List.of(new ClaudeRequest.Message("user", objectMapper.writeValueAsString(puzzle))),
                new ClaudeRequest.OutputConfig(new ClaudeRequest.Format("json_schema", GRID_SCHEMA)));

        ClaudeResponse response = webClient.post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block(TIMEOUT);

        List<String> grid = parseGrid(answerText(response));
        checkShape(grid, puzzle.grid());
        return grid;
    }

    // Claude may think before it answers, so the answer is the text block, not necessarily the first block.
    private String answerText(ClaudeResponse response) {
        if (response == null || response.content() == null) {
            throw new ClaudeAnswerException("Claude sent no answer");
        }
        return response.content().stream()
                .filter(block -> "text".equals(block.type()))
                .map(ClaudeResponse.ContentBlock::text)
                .findFirst()
                .orElseThrow(() -> new ClaudeAnswerException(
                        "Claude sent no text answer (stop_reason: " + response.stopReason() + ")"));
    }

    private List<String> parseGrid(String text) {
        try {
            return objectMapper.readValue(text, GridAnswer.class).grid();
        } catch (JacksonException ex) {
            throw new ClaudeAnswerException("Claude's answer was not valid JSON");
        }
    }

    private void checkShape(List<String> grid, List<String> emptyGrid) {
        boolean sameShape = grid != null && grid.size() == emptyGrid.size();
        for (int row = 0; sameShape && row < grid.size(); row++) {
            sameShape = grid.get(row) != null && grid.get(row).length() == emptyGrid.get(row).length();
        }
        if (!sameShape) {
            throw new ClaudeAnswerException("Claude's grid does not have the same size as the puzzle");
        }
    }

    // The shape of Claude's JSON answer, matching GRID_SCHEMA.
    public record GridAnswer(List<String> grid) {
    }
}
