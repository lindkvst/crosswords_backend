package org.example.crosswords_backend.config;

import org.example.crosswords_backend.gridlogic.GridWord;
import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.example.crosswords_backend.model.GridTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    void isSquare(GridTemplate template) {
        assertEquals(template.getSize(), template.getRows().get(0).length(), template.getName());
    }

    // From 7x7 up a run of 2 would look like a word but get no clue. 5x5 is allowed a few, covered by the other direction.
    @ParameterizedTest
    @MethodSource("templates")
    void hasNoRunsOfTwoFrom7x7Up(GridTemplate template) {
        if (template.getSize() < 7) {
            return;
        }
        List<String> rows = template.getRows();
        List<String> lines = new ArrayList<>(rows);
        for (int col = 0; col < rows.size(); col++) {
            StringBuilder column = new StringBuilder();
            for (String row : rows) {
                column.append(row.charAt(col));
            }
            lines.add(column.toString());
        }
        for (String line : lines) {
            for (String run : line.split("#")) {
                assertTrue(run.length() != 2, template.getName() + " has a run of 2 in " + line);
            }
        }
    }

    @Test
    void namesAndLayoutsAreUnique() {
        assertEquals(templates().size(), templates().stream().map(GridTemplate::getName).distinct().count());
        assertEquals(templates().size(), templates().stream().map(GridTemplate::getLayout).distinct().count());
    }

    @Test
    void everySizeHasAtLeastThreeLayouts() {
        Map<Integer, Long> layoutsBySize = templates().stream()
                .collect(Collectors.groupingBy(GridTemplate::getSize, Collectors.counting()));
        layoutsBySize.forEach((size, count) -> assertTrue(count >= 3, size + "x" + size + " has " + count));
    }

    @ParameterizedTest
    @MethodSource("templates")
    void staysWithinTheFreeTierLimitOf13x13(GridTemplate template) {
        assertTrue(template.getRows().size() <= 13 && template.getRows().get(0).length() <= 13, template.getName());
    }
}
