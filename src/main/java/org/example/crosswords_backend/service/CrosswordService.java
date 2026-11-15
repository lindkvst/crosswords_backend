package org.example.crosswords_backend.service;

import org.example.crosswords_backend.client.ApiClue;
import org.example.crosswords_backend.client.CrosswordApiClient;
import org.example.crosswords_backend.client.FillResponse;
import org.example.crosswords_backend.dto.ClueDto;
import org.example.crosswords_backend.dto.CreateCrosswordRequestDto;
import org.example.crosswords_backend.dto.CrosswordDto;
import org.example.crosswords_backend.dto.CrosswordMapper;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.exception.CrosswordApiException;
import org.example.crosswords_backend.exception.InvalidCrosswordRequestException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.ExternalApi;
import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.CrosswordRepository;
import org.example.crosswords_backend.gridlogic.GridWord;
import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrosswordService {

    private static final String NO_CLUE = "Ingen ledetråd - brug de krydsende bogstaver";
    // The playerPrompt column is a VARCHAR(255). A longer text would fail in the database after the fill was paid for.
    private static final int MAX_PROMPT_LENGTH = 255;

    private final CrosswordApiClient apiClient;
    private final CrosswordRepository crosswordRepository;
    private final GridTemplateService gridTemplateService;
    private final CrosswordMapper mapper;
    private final UserService userService;
    private final ApiCallService apiCallService;

    public CrosswordService(CrosswordApiClient apiClient,
                            CrosswordRepository crosswordRepository,
                            GridTemplateService gridTemplateService,
                            CrosswordMapper mapper,
                            UserService userService,
                            ApiCallService apiCallService) {
        this.apiClient = apiClient;
        this.crosswordRepository = crosswordRepository;
        this.gridTemplateService = gridTemplateService;
        this.mapper = mapper;
        this.userService = userService;
        this.apiCallService = apiCallService;
    }

    public CrosswordDto createCrossword(CreateCrosswordRequestDto request, String username) {
        AppUser user = userService.findEntityByUsername(username);
        // Everything that can fail on our side is checked before the fill, which costs one of the monthly quota.
        GridTemplate template = gridTemplateService.findRandomBySize(request.gridSize());
        String playerPrompt = validatedPrompt(request.playerPrompt());

        Timed<List<String>> answerKeyGrid = fillGrid(template);
        List<GridWord> gridWords = GridWordFinder.findGridWords(answerKeyGrid.result());
        Timed<Map<String, List<ApiClue>>> cluesByWord = fetchClues(gridWords);

        PuzzleDto puzzle = new PuzzleDto(blankOut(answerKeyGrid.result()), toClues(gridWords, cluesByWord.result()));
        Crossword saved = crosswordRepository.save(
                mapper.toEntity(template, user, playerPrompt, puzzle, answerKeyGrid.result()));
        recordTexsCalls(saved, answerKeyGrid.ms(), cluesByWord.ms());
        return mapper.toDto(saved);
    }

    public List<CrosswordDto> findAll() {
        return crosswordRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public CrosswordDto findById(int id) {
        return mapper.toDto(findEntityById(id));
    }

    // For other services only. It returns the entity, which holds the answer key,
    // so a controller must never return the result of this method.
    public Crossword findEntityById(int id) {
        return crosswordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No crossword with id " + id));
    }

    // An empty theme is stored as null. Too long is rejected here, because the column would reject it after the fill.
    private String validatedPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return null;
        }
        String trimmed = prompt.trim();
        if (trimmed.length() > MAX_PROMPT_LENGTH) {
            throw new InvalidCrosswordRequestException("The theme can be at most " + MAX_PROMPT_LENGTH + " characters");
        }
        return trimmed;
    }

    // Each fill costs one of the monthly quota. The filled grid is the answer key.
    private Timed<List<String>> fillGrid(GridTemplate template) {
        Timed<FillResponse> fill = Timed.measure(() -> apiClient.fill(template.getRows()));
        requireFilled(fill.result());
        return new Timed<>(fill.result().grid(), fill.ms());
    }

    private Timed<Map<String, List<ApiClue>>> fetchClues(List<GridWord> gridWords) {
        List<String> words = gridWords.stream().map(GridWord::word).toList();
        return Timed.measure(() -> apiClient.fetchClues(words));
    }

    // After the save, because a usage row must point to a crossword.
    private void recordTexsCalls(Crossword saved, int fillMs, int cluesMs) {
        apiCallService.recordTexsCall(saved, ExternalApi.TEXS_FILL, fillMs);
        apiCallService.recordTexsCall(saved, ExternalApi.TEXS_CLUES, cluesMs);
    }

    private void requireFilled(FillResponse fill) {
        if (fill == null || fill.grid() == null || fill.slotsFilled() < fill.slotsTotal()) {
            String reason = fill == null ? "no response" : fill.reason();
            throw new CrosswordApiException("The crossword API could not fill the grid: " + reason);
        }
    }

    // From here on the answers are dropped: a ClueDto has the word's length, never the word itself.
    private List<ClueDto> toClues(List<GridWord> gridWords, Map<String, List<ApiClue>> cluesByWord) {
        return gridWords.stream()
                .map(gridWord -> new ClueDto(gridWord.number(), gridWord.direction(), gridWord.row(), gridWord.col(),
                        gridWord.word().length(), pickClue(cluesByWord.get(gridWord.word()))))
                .toList();
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
