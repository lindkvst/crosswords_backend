package org.example.crosswords_backend.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class CrosswordApiClient {

    private static final String LANGUAGE = "en";
    // Words scoring lower are left out. Assumption: a higher score means a more ordinary word.
    private static final int MIN_SCORE = 50;
    private static final int MAX_SECONDS = 20;

    private final WebClient webClient;

    // @Qualifier names the bean, because slice 3 adds a second WebClient for Claude.
    public CrosswordApiClient(@Qualifier("crosswordWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public FillResponse fill(List<String> template) {
        FillRequest request = new FillRequest(template, LANGUAGE, MIN_SCORE, MAX_SECONDS);

        // WebClient is non-blocking and returns a Mono (a result that arrives later).
        // Our controllers are ordinary blocking Spring MVC, so block() waits for the answer here.
        // retrieve() throws WebClientResponseException on 4xx/5xx; ControllerExceptionHandler catches it.
        return webClient.post()
                .uri("/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FillResponse.class)
                .block();
    }

    public Map<String, List<ApiClue>> fetchClues(List<String> words) {
        CluesResponse response = webClient.post()
                .uri("/clues/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CluesRequest(words, LANGUAGE))
                .retrieve()
                .bodyToMono(CluesResponse.class)
                .block();

        if (response == null || response.clues() == null) {
            return Map.of();
        }
        return response.clues();
    }
}
