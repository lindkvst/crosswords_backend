package org.example.crosswords_backend.gridlogic;

import org.example.crosswords_backend.dto.ClueDto;
import org.example.crosswords_backend.dto.PuzzleDto;

import java.util.List;
import java.util.Map;

// Turns the filled grid (the answer key) into the puzzle a player or Claude gets: the same grid with every letter
// replaced by '.', and one clue per word with its position and length, never the word itself.
// This is where the rule "the answer key never leaves the backend" is carried out, so it lives in a class
// of its own without Spring or a database, and PuzzleBuilderTest checks it directly.
public class PuzzleBuilder {

    static final String NO_CLUE = "Ingen ledetråd - brug de krydsende bogstaver";

    private PuzzleBuilder() {
    }

    // clueByWord maps an answer word to its clue text. A word without a clue gets NO_CLUE.
    public static PuzzleDto fromAnswerKey(List<String> answerKeyGrid, List<GridWord> gridWords,
                                          Map<String, String> clueByWord) {
        return new PuzzleDto(blankOut(answerKeyGrid), toClues(gridWords, clueByWord));
    }

    private static List<String> blankOut(List<String> grid) {
        return grid.stream().map(row -> row.replaceAll("[^#]", ".")).toList();
    }

    private static List<ClueDto> toClues(List<GridWord> gridWords, Map<String, String> clueByWord) {
        return gridWords.stream()
                .map(gridWord -> new ClueDto(gridWord.number(), gridWord.direction(), gridWord.row(), gridWord.col(),
                        gridWord.word().length(), clueByWord.getOrDefault(gridWord.word(), NO_CLUE)))
                .toList();
    }
}
