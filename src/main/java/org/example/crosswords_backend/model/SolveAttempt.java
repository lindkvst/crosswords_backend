package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Duration;
import java.time.LocalDateTime;

// A player opened a crossword. The start time is the server's clock, so the player's time to solve
// can be measured in the backend, like Claude's, instead of trusting a number from the browser.
@Entity
public class SolveAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "crossword_id", nullable = false)
    private Crossword crossword;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime startedOn;

    protected SolveAttempt() {
        // required by JPA
    }

    public SolveAttempt(Crossword crossword, AppUser user) {
        this.crossword = crossword;
        this.user = user;
        this.startedOn = LocalDateTime.now();
    }

    // Capped at Integer.MAX_VALUE (about 24 days), because timeToSolveMs is an int.
    public int elapsedMs() {
        long ms = Duration.between(startedOn, LocalDateTime.now()).toMillis();
        return (int) Math.min(ms, Integer.MAX_VALUE);
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

    public LocalDateTime getStartedOn() {
        return startedOn;
    }
}
