package org.example.crosswords_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // One WebClient per external service. Base URL and API key are set once here,
    // so no other class ever touches the key.
    // Only created when the real API is used, so the fake mode needs no API key.
    @Bean
    @ConditionalOnProperty(name = "app.crossword-source", havingValue = "texs", matchIfMissing = true)
    public WebClient crosswordWebClient(@Value("${crossword.api.base-url}") String baseUrl,
                                        @Value("${crossword.api.key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.crossword-solver", havingValue = "claude", matchIfMissing = true)
    public WebClient claudeWebClient(@Value("${anthropic.api.base-url}") String baseUrl,
                                     @Value("${anthropic.api.key}") String apiKey,
                                     @Value("${anthropic.api.version}") String apiVersion) {
        // Same pattern as above. Anthropic also requires a version header on every request.
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.set("anthropic-version", apiVersion);
                })
                .build();
    }
}
