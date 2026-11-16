package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.ExternalApi;

import java.time.LocalDateTime;

public record ApiCallDto(
        int id,
        ExternalApi apiName,
        ClaudeModel llmModel,
        int durationMs,
        Integer inputTokens,
        Integer outputTokens,
        double estimatedWh,
        LocalDateTime createdOn
) {
}
