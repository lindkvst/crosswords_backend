package org.example.crosswords_backend.service;

import org.example.crosswords_backend.exception.DailyLimitException;
import org.example.crosswords_backend.repository.CrosswordRepository;
import org.example.crosswords_backend.repository.CrosswordSubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Protects the money: each new crossword uses one of texs' 30 fills a month, and each Claude call costs credit.
// Counted from the saved rows, so nothing new is stored. A failed fill saves no crossword and is not counted.
@Service
public class DailyLimitService {

    private final CrosswordRepository crosswordRepository;
    private final CrosswordSubmissionRepository submissionRepository;
    private final int crosswordsPerDay;
    private final int claudeSolvesPerDay;

    public DailyLimitService(CrosswordRepository crosswordRepository,
                             CrosswordSubmissionRepository submissionRepository,
                             @Value("${app.limits.crosswords-per-day}") int crosswordsPerDay,
                             @Value("${app.limits.claude-solves-per-day}") int claudeSolvesPerDay) {
        this.crosswordRepository = crosswordRepository;
        this.submissionRepository = submissionRepository;
        this.crosswordsPerDay = crosswordsPerDay;
        this.claudeSolvesPerDay = claudeSolvesPerDay;
    }

    public void requireCrosswordAllowed(int userId) {
        if (crosswordRepository.countByUserIdAndCreatedOnAfter(userId, startOfToday()) >= crosswordsPerDay) {
            throw new DailyLimitException("Daily limit reached: " + crosswordsPerDay + " new crosswords per day");
        }
    }

    public void requireClaudeAllowed(int userId) {
        if (submissionRepository.countByUserIdAndLlmModelIsNotNullAndCreatedOnAfter(userId, startOfToday())
                >= claudeSolvesPerDay) {
            throw new DailyLimitException("Daily limit reached: " + claudeSolvesPerDay + " Claude solves per day");
        }
    }

    private LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }
}
