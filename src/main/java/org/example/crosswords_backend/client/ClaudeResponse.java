package org.example.crosswords_backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Only the fields we use. The answer is a list of content blocks, e.g. a thinking block and a text block.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaudeResponse(List<ContentBlock> content, @JsonProperty("stop_reason") String stopReason) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(String type, String text) {
    }
}
