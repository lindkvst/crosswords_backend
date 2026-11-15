package org.example.crosswords_backend.config;

import org.example.crosswords_backend.gridlogic.GridWord;
import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.example.crosswords_backend.model.GridTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A broken layout would only show up when texs is called, which costs a fill. These checks are free.
class InitDataTemplatesTest {

    static List<GridTemplate> templates() {
        return InitData.templates();
    }

    @ParameterizedTest
    @MethodSource("templates")
    void allRowsHaveTheSameLength(GridTemplate template) {
        List<String> rows = template.getRows();
        rows.forEach(row -> assertEquals(rows.get(0).length(), row.length(), template.getName()));
    }

    @ParameterizedTest
    @MethodSource("templates")
    void isSymmetricWhenTurned180Degrees(GridTemplate template) {
        List<String> rows = template.getRows();
        int last = rows.size() - 1;
        for (int row = 0; row <= last; row++) {
            String turned = new StringBuilder(rows.get(last - row)).reverse().toString();
            assertEquals(turned, rows.get(row), template.getName() + " row " + row);
        }
    }

    // Every white cell must be part of at least one word that GridWordFinder finds, or it would get no clue.
    @ParameterizedTest
    @MethodSource("templates")
    void everyWhiteCellIsPartOfAWord(GridTemplate template) {
        List<String> rows = template.getRows();
        Set<String> covered = new HashSet<>();
        for (GridWord word : GridWordFinder.findGridWords(rows)) {
            for (int i = 0; i < word.word().length(); i++) {
                boolean across = word.direction().equals("across");
                covered.add(across ? word.row() + "," + (word.col() + i) : (word.row() + i) + "," + word.col());
            }
        }
        for (int row = 0; row < rows.size(); row++) {
            for (int col = 0; col < rows.get(row).length(); col++) {
                if (rows.get(row).charAt(col) == '.') {
                    assertTrue(covered.contains(row + "," + col), template.getName() + " cell " + row + "," + col);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("templates")
    void staysWithinTheFreeTierLimitOf13x13(GridTemplate template) {
        assertTrue(template.getRows().size() <= 13 && template.getRows().get(0).length() <= 13, template.getName());
    }
}
