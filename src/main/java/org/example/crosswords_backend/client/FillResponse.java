package org.example.crosswords_backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Only the fields we use. The API sends more (sessionId, quality), which we ignore.
@JsonIgnoreProperties(ignoreUnknown = true)
public record FillResponse(
        List<String> grid,
        int slotsFilled,
        int slotsTotal,
        String reason
) {
}
