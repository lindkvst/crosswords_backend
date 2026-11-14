package org.example.crosswords_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // One WebClient per external service. Base URL and API key are set once here,
    // so no other class ever touches the key.
    @Bean
    public WebClient crosswordWebClient(@Value("${crossword.api.base-url}") String baseUrl,
                                        @Value("${crossword.api.key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }
}
