package org.example.crosswords_backend.client;

import java.util.List;
import java.util.Map;

// Where filled grids and clues come from. CrosswordService depends on this interface, not on texs (Dependency
// Inversion), so the source can be swapped without touching the service: texs in normal use, a fake during
// development (saves the 30 fills a month), a mock in the tests. app.crossword-source picks the implementation.
public interface CrosswordSource {

    FillResponse fill(List<String> template);

    // Clues keyed by answer word. Words without any clue are missing from the map.
    Map<String, List<ApiClue>> fetchClues(List<String> words);
}
