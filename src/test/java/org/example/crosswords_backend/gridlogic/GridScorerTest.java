package org.example.crosswords_backend.gridlogic;

import org.example.crosswords_backend.exception.InvalidSubmissionException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridScorerTest {

    private static final List<String> ANSWER_KEY = List.of("AB#", "CDE");

    @Test
    void marksCorrectWrongEmptyAndBlackCells() {
        List<String> marks = GridScorer.markCells(ANSWER_KEY, List.of("AX#", "C.E"));

        assertEquals(List.of("+-#", "+.+"), marks);
    }

    @Test
    void acceptsLowercaseLetters() {
        assertEquals(List.of("++#", "+++"), GridScorer.markCells(ANSWER_KEY, List.of("ab#", "cde")));
    }

    @Test
    void percentIgnoresBlackCellsAndCountsEmptyAsNotCorrect() {
        // 5 letter cells, 3 correct (the empty and the wrong one are not).
        assertEquals(60, GridScorer.percentCorrect(List.of("+-#", "+.+")));
    }

    @Test
    void rejectsWrongNumberOfRows() {
        assertThrows(InvalidSubmissionException.class, () -> GridScorer.markCells(ANSWER_KEY, List.of("AB#")));
    }

    @Test
    void rejectsRowOfWrongLength() {
        assertThrows(InvalidSubmissionException.class, () -> GridScorer.markCells(ANSWER_KEY, List.of("AB#", "CD")));
    }

    @Test
    void rejectsMissingGrid() {
        assertThrows(InvalidSubmissionException.class, () -> GridScorer.markCells(ANSWER_KEY, null));
    }
}
