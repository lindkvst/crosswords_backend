package org.example.crosswords_backend.client;

import org.example.crosswords_backend.exception.CrosswordApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

// The real crossword source (texs). Used unless app.crossword-source is set to something else, e.g. "fake".
@Component
@ConditionalOnProperty(name = "app.crossword-source", havingValue = "texs", matchIfMissing = true)
public class CrosswordApiClient implements CrosswordSource {

    // Words scoring lower are left out. Assumption: a higher score means a more ordinary word.
    private static final int MIN_SCORE = 50;
    private static final int MAX_SECONDS = 20;
    // WebClient waits forever by default. texs gets MAX_SECONDS to fill, plus room for the network.
    // Without a limit, a stuck texs would keep the request (and a server thread) busy until the connection dies.
    private static final Duration TIMEOUT = Duration.ofSeconds(MAX_SECONDS + 20);

    private final WebClient webClient;
    private final String language;

    // @Qualifier names the bean, because slice 3 adds a second WebClient for Claude.
    public CrosswordApiClient(@Qualifier("crosswordWebClient") WebClient webClient,
                              @Value("${crossword.api.language}") String language) {
        this.webClient = webClient;
        this.language = language;
    }

    @Override
    public FillResponse fill(List<String> template) {
        FillRequest request = new FillRequest(template, language, MIN_SCORE, MAX_SECONDS);

        // WebClient is non-blocking and returns a Mono (a result that arrives later).
        // Our controllers are ordinary blocking Spring MVC, so block() waits for the answer here.
        // retrieve() throws WebClientResponseException on 4xx/5xx; ControllerExceptionHandler catches it.
        return webClient.post()
                .uri("/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FillResponse.class)
                .timeout(TIMEOUT, Mono.error(timedOut()))
                .block();
    }

    @Override
    public Map<String, List<ApiClue>> fetchClues(List<String> words) {
        CluesResponse response = webClient.post()
                .uri("/clues/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CluesRequest(words, language))
                .retrieve()
                .bodyToMono(CluesResponse.class)
                .timeout(TIMEOUT, Mono.error(timedOut()))
                .block();

        if (response == null || response.clues() == null) {
            return Map.of();
        }
        return response.clues();
    }

    // Becomes 502 Bad Gateway in ControllerExceptionHandler: the fault is upstream, not in the client's request.
    private CrosswordApiException timedOut() {
        return new CrosswordApiException("The crossword API did not answer within " + TIMEOUT.toSeconds() + " seconds");
    }
}
