package org.example.crosswords_backend.gridlogic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridWordFinderTest {

    @Test
    void findsAndNumbersWordsLikeAPrintedCrossword() {
        List<String> grid = List.of(
                "CAT",
                "A#O",
                "BEE");

        List<GridWord> words = GridWordFinder.findGridWords(grid);

        // Across and down from the same cell share number 1; numbering goes left to right, top to bottom.
        assertEquals(List.of(
                new GridWord(1, "across", 0, 0, "CAT"),
                new GridWord(1, "down", 0, 0, "CAB"),
                new GridWord(2, "down", 0, 2, "TOE"),
                new GridWord(3, "across", 2, 0, "BEE")), words);
    }

    @Test
    void ignoresRunsShorterThanThree() {
        // Row 0 has a run of 2 ("AB"), which is not a word. Only the down words count.
        List<String> grid = List.of(
                "AB#",
                "CDE",
                "FGH");

        List<String> found = GridWordFinder.findGridWords(grid).stream().map(GridWord::word).toList();

        assertEquals(List.of("ACF", "BDG", "CDE", "FGH"), found);
    }
}
