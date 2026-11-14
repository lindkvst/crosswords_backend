package org.example.crosswords_backend.gridlogic;

import java.util.ArrayList;
import java.util.List;

// The crossword API returns only a filled grid. This class finds the words in it
// and numbers them the way printed crosswords do: left to right, top to bottom.
public class GridWordFinder {

    // The API does not treat runs shorter than 3 letters as words, so neither do we.
    private static final int MIN_LENGTH = 3;

    public static List<GridWord> findGridWords(List<String> grid) {
        List<GridWord> gridWords = new ArrayList<>();
        int number = 0;

        for (int row = 0; row < grid.size(); row++) {
            for (int col = 0; col < grid.get(row).length(); col++) {
                if (isBlack(grid, row, col)) {
                    continue;
                }
                // A word starts here if the cell before it (left or above) is black or outside the grid.
                String across = isBlack(grid, row, col - 1) ? readWord(grid, row, col, 0, 1) : "";
                String down = isBlack(grid, row - 1, col) ? readWord(grid, row, col, 1, 0) : "";
                boolean startsAcross = across.length() >= MIN_LENGTH;
                boolean startsDown = down.length() >= MIN_LENGTH;

                // An across and a down word starting in the same cell share one number.
                if (startsAcross || startsDown) {
                    number++;
                }
                if (startsAcross) {
                    gridWords.add(new GridWord(number, "across", row, col, across));
                }
                if (startsDown) {
                    gridWords.add(new GridWord(number, "down", row, col, down));
                }
            }
        }
        return gridWords;
    }

    private static boolean isBlack(List<String> grid, int row, int col) {
        boolean outside = row < 0 || row >= grid.size() || col < 0 || col >= grid.get(row).length();
        return outside || grid.get(row).charAt(col) == '#';
    }

    private static String readWord(List<String> grid, int row, int col, int rowStep, int colStep) {
        StringBuilder word = new StringBuilder();
        while (!isBlack(grid, row, col)) {
            word.append(grid.get(row).charAt(col));
            row += rowStep;
            col += colStep;
        }
        return word.toString();
    }
}
