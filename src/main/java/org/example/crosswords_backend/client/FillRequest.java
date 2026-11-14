package org.example.crosswords_backend.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Body of POST /fill. The crossword API expects snake_case names in requests.
public record FillRequest(
        List<String> grid,
        String language,
        @JsonProperty("min_score") int minScore,
        @JsonProperty("max_time") int maxTime
) {
}
