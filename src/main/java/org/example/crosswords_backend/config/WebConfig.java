package org.example.crosswords_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DailyLimitInterceptor dailyLimitInterceptor;

    public WebConfig(DailyLimitInterceptor dailyLimitInterceptor) {
        this.dailyLimitInterceptor = dailyLimitInterceptor;
    }

    // Only the two endpoints that cost money: a new crossword (texs fill) and a Claude solve.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dailyLimitInterceptor)
                .addPathPatterns("/api/crosswords", "/api/crosswords/*/submissions/claude");
    }
}
