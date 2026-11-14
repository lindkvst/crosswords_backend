package org.example.crosswords_backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

// Clues are keyed by answer word. Words without any clue are missing from the map.
@JsonIgnoreProperties(ignoreUnknown = true)
public record CluesResponse(Map<String, List<ApiClue>> clues) {
}
