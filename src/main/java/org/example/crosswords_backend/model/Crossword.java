package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Crossword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "grid_template_id")
    private GridTemplate gridTemplate;

    // Who created it. Empty for crosswords made before V2.0.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    // The theme the player typed. Empty when none was given, and for crosswords from before V3.0.
    private String playerPrompt;

    // The puzzle without answers, stored exactly as it is sent out.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String emptyPuzzleJson;

    // The answer key: the grid filled with the correct letters. Never leaves the backend.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answerKeyJson;

    private LocalDateTime createdOn;

    protected Crossword() {
        // required by JPA
    }

    public Crossword(GridTemplate gridTemplate, AppUser user, String playerPrompt,
                     String emptyPuzzleJson, String answerKeyJson) {
        this.gridTemplate = gridTemplate;
        this.user = user;
        this.playerPrompt = playerPrompt;
        this.emptyPuzzleJson = emptyPuzzleJson;
        this.answerKeyJson = answerKeyJson;
        this.createdOn = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public GridTemplate getGridTemplate() {
        return gridTemplate;
    }

    public AppUser getUser() {
        return user;
    }

    public String getPlayerPrompt() {
        return playerPrompt;
    }

    public String getEmptyPuzzleJson() {
        return emptyPuzzleJson;
    }

    public String getAnswerKeyJson() {
        return answerKeyJson;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}
