package org.example.crosswords_backend.service;

import org.example.crosswords_backend.client.ApiClue;
import org.example.crosswords_backend.client.CrosswordApiClient;
import org.example.crosswords_backend.client.FillResponse;
import org.example.crosswords_backend.dto.ClueDto;
import org.example.crosswords_backend.dto.CrosswordDto;
import org.example.crosswords_backend.dto.CrosswordMapper;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.exception.CrosswordApiException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.CrosswordRepository;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.example.crosswords_backend.gridlogic.GridWord;
import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrosswordService {

    private static final String NO_CLUE = "No clue available - use the crossing letters";

    private final CrosswordApiClient apiClient;
    private final CrosswordRepository crosswordRepository;
    private final GridTemplateRepository gridTemplateRepository;
    private final CrosswordMapper mapper;

    public CrosswordService(CrosswordApiClient apiClient,
                            CrosswordRepository crosswordRepository,
                            GridTemplateRepository gridTemplateRepository,
                            CrosswordMapper mapper) {
        this.apiClient = apiClient;
        this.crosswordRepository = crosswordRepository;
        this.gridTemplateRepository = gridTemplateRepository;
        this.mapper = mapper;
    }

    public CrosswordDto createCrossword() {
        GridTemplate template = gridTemplateRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("No grid template in the database"));

        // Each call costs one fill of the monthly quota.
        // Her fyldes et tomt grid ud med ord
        FillResponse fill = apiClient.fill(template.getRows());
        if (fill == null || fill.grid() == null || fill.slotsFilled() < fill.slotsTotal()) {
            String reason = fill == null ? "no response" : fill.reason();
            throw new CrosswordApiException("The crossword API could not fill the grid: " + reason);
        }

        List<String> answerKeyGrid = fill.grid();
        List<GridWord> gridWords = GridWordFinder.findGridWords(answerKeyGrid);
        List<String> words = gridWords.stream().map(GridWord::word).toList();
        Map<String, List<ApiClue>> cluesByWord = apiClient.fetchClues(words);

        // From here on the answers are dropped: a ClueDto has no word, and the grid is blanked.
        List<ClueDto> clues = gridWords.stream()
                .map(gridWord -> new ClueDto(gridWord.number(), gridWord.direction(), gridWord.row(), gridWord.col(),
                        gridWord.word().length(), pickClue(cluesByWord.get(gridWord.word()))))
                .toList();
        PuzzleDto puzzle = new PuzzleDto(blankOut(answerKeyGrid), clues);

        Crossword saved = crosswordRepository.save(mapper.toEntity(template, puzzle, answerKeyGrid));
        return mapper.toDto(saved);
    }

    public List<CrosswordDto> findAll() {
        return crosswordRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public CrosswordDto findById(int id) {
        Crossword crossword = crosswordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No crossword with id " + id));
        return mapper.toDto(crossword);
    }

    private String pickClue(List<ApiClue> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return NO_CLUE;
        }
        return candidates.get(0).text();
    }

    private List<String> blankOut(List<String> grid) {
        return grid.stream().map(row -> row.replaceAll("[^#]", ".")).toList();
    }
}
