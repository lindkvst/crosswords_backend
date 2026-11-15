package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    // Who submitted it, or who asked Claude to solve it. Empty for submissions made before V2.0.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    // The submitted grid, in the same row format as the answer key.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String submittedGridJson;

    // Which Claude model solved it. Empty when a player solved it.
    @Enumerated(EnumType.STRING)
    private ClaudeModel llmModel;

    private int timeToSolveMs;

    // Percentage of letter cells that match the answer key, 0-100. Always computed in the backend.
    private int percentCorrect;

    private LocalDateTime createdOn;

    protected CrosswordSubmission() {
        // required by JPA
    }

    public CrosswordSubmission(Crossword crossword, AppUser user, String submittedGridJson,
                             ClaudeModel llmModel, int timeToSolveMs, int percentCorrect) {
        this.crossword = crossword;
        this.user = user;
        this.submittedGridJson = submittedGridJson;
        this.llmModel = llmModel;
        this.timeToSolveMs = timeToSolveMs;
        this.percentCorrect = percentCorrect;
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

    public ClaudeModel getLlmModel() {
        return llmModel;
    }

    public int getTimeToSolveMs() {
        return timeToSolveMs;
    }

    public int getPercentCorrect() {
        return percentCorrect;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
