package org.example.crosswords_backend.gridlogic;

import org.example.crosswords_backend.exception.InvalidSubmissionException;

import java.util.ArrayList;
import java.util.List;

// Compares a submitted grid with the answer key, cell by cell.
public class GridScorer {

    // One mark per cell, in the same row format as the grid.
    public static final char CORRECT = '+';
    public static final char WRONG = '-';
    public static final char EMPTY = '.';
    public static final char BLACK = '#';

    // Returns one string per row, e.g. "+-#+." for correct, wrong, black, correct, empty.
    public static List<String> markCells(List<String> answerKeyGrid, List<String> submittedGrid) {
        requireSameRowCount(answerKeyGrid, submittedGrid);
        List<String> marks = new ArrayList<>();
        for (int row = 0; row < answerKeyGrid.size(); row++) {
            marks.add(markRow(row, answerKeyGrid.get(row), submittedGrid.get(row)));
        }
        return marks;
    }

    // Percentage of letter cells that are correct, 0-100. An empty cell counts as not correct.
    // Black cells are not part of the puzzle, so they count neither for nor against.
    public static int percentCorrect(List<String> cellMarks) {
        String allMarks = String.join("", cellMarks);
        long letterCells = allMarks.chars().filter(mark -> mark != BLACK).count();
        long correctCells = allMarks.chars().filter(mark -> mark == CORRECT).count();
        return letterCells == 0 ? 0 : Math.round(100f * correctCells / letterCells);
    }

    private static void requireSameRowCount(List<String> answerKeyGrid, List<String> submittedGrid) {
        if (submittedGrid == null || submittedGrid.size() != answerKeyGrid.size()) {
            throw new InvalidSubmissionException("The grid must have " + answerKeyGrid.size() + " rows");
        }
    }

    private static String markRow(int row, String answerRow, String submittedRow) {
        if (submittedRow == null || submittedRow.length() != answerRow.length()) {
            throw new InvalidSubmissionException("Row " + row + " must have " + answerRow.length() + " cells");
        }
        StringBuilder rowMarks = new StringBuilder();
        for (int col = 0; col < answerRow.length(); col++) {
            rowMarks.append(markCell(answerRow.charAt(col), submittedRow.charAt(col)));
        }
        return rowMarks.toString();
    }

    private static char markCell(char expected, char submitted) {
        if (expected == BLACK) {
            return BLACK;
        }
        if (submitted == EMPTY) {
            return EMPTY;
        }
        return Character.toUpperCase(submitted) == expected ? CORRECT : WRONG;
    }
}
