package org.example.crosswords_backend.service;

import org.example.crosswords_backend.client.ClaudeSolution;
import org.example.crosswords_backend.dto.ApiCallDto;
import org.example.crosswords_backend.dto.ApiUsageSummaryDto;
import org.example.crosswords_backend.model.ApiCall;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.CrosswordSubmission;
import org.example.crosswords_backend.model.ExternalApi;
import org.example.crosswords_backend.repository.ApiCallRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Records what each external call cost. Only successful calls are recorded, because a failed call
// has no crossword or submission to belong to.
@Service
public class ApiCallService {

    private final ApiCallRepository apiCallRepository;

    public ApiCallService(ApiCallRepository apiCallRepository) {
        this.apiCallRepository = apiCallRepository;
    }

    public void recordTexsCall(Crossword crossword, ExternalApi apiName, int durationMs) {
        apiCallRepository.save(new ApiCall(crossword, null, apiName, null, durationMs, null, null,
                EnergyEstimator.texsWh(durationMs)));
    }

    public void recordClaudeCall(Crossword crossword, CrosswordSubmission submission, ClaudeModel model,
                                 int durationMs, ClaudeSolution solution) {
        apiCallRepository.save(new ApiCall(crossword, submission, ExternalApi.CLAUDE_SOLVE, model, durationMs,
                solution.inputTokens(), solution.outputTokens(), EnergyEstimator.claudeWh(model, durationMs)));
    }

    public List<ApiCallDto> findByCrossword(int crosswordId) {
        return apiCallRepository.findByCrosswordIdOrderByCreatedOnAsc(crosswordId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ApiUsageSummaryDto> summarizeByApi() {
        return apiCallRepository.summarizeByApi();
    }

    private ApiCallDto toDto(ApiCall call) {
        return new ApiCallDto(call.getId(), call.getApiName(), call.getLlmModel(), call.getDurationMs(),
                call.getInputTokens(), call.getOutputTokens(), call.getEstimatedWh(), call.getCreatedOn());
    }
}
