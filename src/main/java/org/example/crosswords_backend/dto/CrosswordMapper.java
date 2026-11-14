package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.GridTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

// Translates between the entity (JSON text in the database) and the DTOs.
// The only class that knows the crossword is stored as JSON.
@Component
public class CrosswordMapper {

    private final ObjectMapper objectMapper;

    public CrosswordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Crossword toEntity(GridTemplate template, PuzzleDto puzzle, List<String> answerKeyGrid) {
        return new Crossword(
                template,
                null,
                objectMapper.writeValueAsString(puzzle),
                objectMapper.writeValueAsString(answerKeyGrid));
    }

    // Only for scoring inside the backend. Never put the result in a DTO.
    public List<String> toAnswerKeyGrid(Crossword crossword) {
        return List.of(objectMapper.readValue(crossword.getAnswerKeyJson(), String[].class));
    }

    // Built only from emptyPuzzleJson, so the answer key cannot end up in a response by accident.
    public CrosswordDto toDto(Crossword crossword) {
        PuzzleDto puzzle = objectMapper.readValue(crossword.getEmptyPuzzleJson(), PuzzleDto.class);
        return new CrosswordDto(crossword.getId(), crossword.getCreatedOn(), puzzle);
    }
}
