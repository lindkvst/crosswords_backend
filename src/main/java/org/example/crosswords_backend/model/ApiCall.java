package org.example.crosswords_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

// One successful call to an external API, with what it cost in time, tokens and estimated energy.
@Entity
public class ApiCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Every call is made for one crossword: texs to create it, Claude to solve it.
    @ManyToOne
    @JoinColumn(name = "crossword_id", nullable = false)
    private Crossword crossword;

    // Only set for Claude: the submission its answer became.
    @ManyToOne
    @JoinColumn(name = "submission_id")
    private CrosswordSubmission submission;

    @Enumerated(EnumType.STRING)
    private ExternalApi apiName;

    // Only set for Claude.
    @Enumerated(EnumType.STRING)
    private ClaudeModel llmModel;

    // Measured in the backend around the call.
    private int durationMs;

    // Reported by Anthropic. Null for texs, which has no tokens.
    private Integer inputTokens;
    private Integer outputTokens;

    // Watts x time, see EnergyEstimator. An estimate, not a measurement.
    private double estimatedWh;

    private LocalDateTime createdOn;

    protected ApiCall() {
        // required by JPA
    }

    public ApiCall(Crossword crossword, CrosswordSubmission submission, ExternalApi apiName, ClaudeModel llmModel,
                   int durationMs, Integer inputTokens, Integer outputTokens, double estimatedWh) {
        this.crossword = crossword;
        this.submission = submission;
        this.apiName = apiName;
        this.llmModel = llmModel;
        this.durationMs = durationMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.estimatedWh = estimatedWh;
        this.createdOn = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Crossword getCrossword() {
        return crossword;
    }

    public CrosswordSubmission getSubmission() {
        return submission;
    }

    public ExternalApi getApiName() {
        return apiName;
    }

    public ClaudeModel getLlmModel() {
        return llmModel;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public double getEstimatedWh() {
        return estimatedWh;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
