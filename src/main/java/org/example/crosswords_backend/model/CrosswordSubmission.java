package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

// One submitted attempt at a crossword, by a player (slice 2) or by Claude (slice 3).
@Entity
public class CrosswordSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "crossword_id", nullable = false)
    private Crossword crossword;

    // Empty until users exist in V2.0.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    // The submitted grid, in the same row format as the answer key.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String submittedGridJson;

    private boolean llmAssisted;

    private int timeToSolveMs;

    // Percentage of letter cells that match the answer key, 0-100. Always computed in the backend.
    private int score;

    private LocalDateTime createdOn;

    protected CrosswordSubmission() {
        // required by JPA
    }

    public CrosswordSubmission(Crossword crossword, AppUser user, String submittedGridJson,
                             boolean llmAssisted, int timeToSolveMs, int score) {
        this.crossword = crossword;
        this.user = user;
        this.submittedGridJson = submittedGridJson;
        this.llmAssisted = llmAssisted;
        this.timeToSolveMs = timeToSolveMs;
        this.score = score;
        this.createdOn = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Crossword getCrossword() {
        return crossword;
    }

    public AppUser getUser() {
        return user;
    }

    public String getSubmittedGridJson() {
        return submittedGridJson;
    }

    public boolean isLlmAssisted() {
        return llmAssisted;
    }

    public int getTimeToSolveMs() {
        return timeToSolveMs;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
