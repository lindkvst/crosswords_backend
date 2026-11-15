package org.example.crosswords_backend.gridlogic;

import org.example.crosswords_backend.dto.ClueDto;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// The answer-key rule as a plain unit test: no Spring, no database, no mocks.
class PuzzleBuilderTest {

    private static final List<String> ANSWER_KEY = List.of("AB#CD", "EFGHI", "JKLMN", "OPQRS", "TU#VW");

    @Test
    void theGridKeepsBlackCellsAndLosesEveryLetter() {
        PuzzleDto puzzle = build(Map.of());

        assertEquals(List.of("..#..", ".....", ".....", ".....", "..#.."), puzzle.grid());
    }

    @Test
    void noAnswerWordAppearsAnywhereInThePuzzle() {
        PuzzleDto puzzle = build(Map.of("EFGHI", "En ledetråd"));

        String everything = puzzle.toString();
        for (GridWord word : GridWordFinder.findGridWords(ANSWER_KEY)) {
            assertFalse(everything.contains(word.word()), "answer word leaked: " + word.word());
        }
    }

    @Test
    void aClueHasTheWordsPositionAndLengthAndTheClueText() {
        ClueDto clue = clueFor(build(Map.of("EFGHI", "En ledetråd")), 1, 0, "across");

        assertEquals(5, clue.length());
        assertEquals("En ledetråd", clue.text());
    }

    @Test
    void aWordWithoutAClueGetsTheNoClueText() {
        ClueDto clue = clueFor(build(Map.of()), 1, 0, "across");

        assertEquals(PuzzleBuilder.NO_CLUE, clue.text());
    }

    private PuzzleDto build(Map<String, String> clueByWord) {
        return PuzzleBuilder.fromAnswerKey(ANSWER_KEY, GridWordFinder.findGridWords(ANSWER_KEY), clueByWord);
    }

    private ClueDto clueFor(PuzzleDto puzzle, int row, int col, String direction) {
        return puzzle.clues().stream()
                .filter(clue -> clue.row() == row && clue.col() == col && clue.direction().equals(direction))
                .findFirst().orElseThrow();
    }
}
