package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

// An empty grid shape that the crossword API fills with words.
@Entity
public class GridTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    // Rows separated by '/'. In a row, '.' is a cell to fill and '#' is a black cell.
    @Column(nullable = false)
    private String layout;

    protected GridTemplate() {
        // required by JPA
    }

    public GridTemplate(String name, String layout) {
        this.name = name;
        this.layout = layout;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLayout() {
        return layout;
    }

    // The format the crossword API expects: one string per row.
    public List<String> getRows() {
        return List.of(layout.split("/"));
    }
}
