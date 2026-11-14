package org.example.crosswords_backend.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// Body of POST /v1/messages. The nested records mirror the nested JSON.
public record ClaudeRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Message> messages,
        @JsonProperty("output_config") OutputConfig outputConfig
) {

    public record Message(String role, String content) {
    }

    public record OutputConfig(Format format) {
    }

    // Structured outputs: Claude must answer with JSON that matches this JSON schema.
    public record Format(String type, Map<String, Object> schema) {
    }
}
