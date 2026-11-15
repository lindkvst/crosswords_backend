package org.example.crosswords_backend.client;

import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Development only (app.crossword-source=fake): fills the template locally instead of calling texs, so trying out
// the frontend costs no fills and needs no API key. The "words" are letters A-Z in turn, not real Danish words.
@Component
@ConditionalOnProperty(name = "app.crossword-source", havingValue = "fake")
public class FakeCrosswordSource implements CrosswordSource {

    @Override
    public FillResponse fill(List<String> template) {
        int[] next = {0};
        List<String> grid = template.stream()
                .map(row -> row.chars()
                        .mapToObj(cell -> cell == '#' ? "#" : String.valueOf((char) ('A' + next[0]++ % 26)))
                        .collect(Collectors.joining()))
                .toList();
        int slots = GridWordFinder.findGridWords(grid).size();
        return new FillResponse(grid, slots, slots, null);
    }

    @Override
    public Map<String, List<ApiClue>> fetchClues(List<String> words) {
        return words.stream().distinct().collect(Collectors.toMap(
                word -> word,
                word -> List.of(new ApiClue("Testledetråd med " + word.length() + " bogstaver", "fake"))));
    }
}
