package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.ExternalApi;

// Totals for one external API. Long and Double because that is what SQL COUNT and SUM return.
// The token sums are null for texs, which has no tokens.
public record ApiUsageSummaryDto(
        ExternalApi apiName,
        Long calls,
        Long totalDurationMs,
        Long totalInputTokens,
        Long totalOutputTokens,
        Double totalEstimatedWh
) {
}
