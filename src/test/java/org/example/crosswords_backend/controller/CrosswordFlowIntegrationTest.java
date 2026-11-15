package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.client.ApiClue;
import org.example.crosswords_backend.client.ClaudeSolution;
import org.example.crosswords_backend.client.FillResponse;
import org.example.crosswords_backend.dto.PuzzleDto;
import org.example.crosswords_backend.gridlogic.GridWord;
import org.example.crosswords_backend.gridlogic.GridWordFinder;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Creating and solving a crossword end to end, with texs and Claude mocked.
class CrosswordFlowIntegrationTest extends IntegrationTestBase {

    private static final String PLAYER = "flow-player";
    private static final String ADMIN = "flow-admin";

    // What the mocked texs "fills" a 5x5 grid with, whichever 5x5 layout the backend picked. These letters are the answer key.
    private static final List<String> ANSWER_KEY = List.of("AB#CD", "EFGHI", "JKLMN", "OPQRS", "TU#VW");

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ensureUser(PLAYER, Role.PLAYER);
        ensureUser(ADMIN, Role.ADMIN);
        when(crosswordApiClient.fill(anyList())).thenReturn(new FillResponse(ANSWER_KEY, 8, 8, null));
        when(crosswordApiClient.fetchClues(anyList()))
                .thenReturn(Map.of("AEJOT", List.of(new ApiClue("En ledetråd", "test"))));
    }

    @Test
    void createdCrosswordNeverContainsTheAnswerKey() throws Exception {
        String body = createCrossword("dyr i skoven");

        for (String row : ANSWER_KEY) {
            assertFalse(body.contains(row), "answer row leaked: " + row);
        }
        for (GridWord word : GridWordFinder.findGridWords(ANSWER_KEY)) {
            assertFalse(body.contains(word.word()), "answer word leaked: " + word.word());
        }
        JsonNode crossword = objectMapper.readTree(body);
        assertTrue(crossword.get("theme").asText().equals("dyr i skoven"));
        assertTrue(crossword.get("puzzle").get("grid").get(1).asText().equals("....."));
    }

    @Test
    void creatingACrosswordRecordsBothTexsCalls() throws Exception {
        int id = idOf(createCrossword(null));

        mockMvc.perform(get("/api/crosswords/" + id + "/api-calls").with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].apiName").value("TEXS_FILL"))
                .andExpect(jsonPath("$[1].apiName").value("TEXS_CLUES"));
    }

    @Test
    void unknownSizeGives404WithoutUsingTheQuota() throws Exception {
        mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridSize\": 6}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(crosswordApiClient);
    }

    @Test
    void sizesListsEachSizeOnceWithItsNumberOfLayouts() throws Exception {
        mockMvc.perform(get("/api/grid-templates/sizes").with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].size").value(5))
                .andExpect(jsonPath("$[0].layouts").value(6))
                .andExpect(jsonPath("$[3].size").value(13));
    }

    @Test
    void claudeGetsOnlyTheEmptyPuzzleAndIsScoredInTheBackend() throws Exception {
        int id = idOf(createCrossword(null));
        when(claudeApiClient.solveCrossword(any(), eq(ClaudeModel.CHEAP_AND_DUMB)))
                .thenReturn(new ClaudeSolution(ANSWER_KEY, 1000, 200));

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions/claude")
                        .param("model", "CHEAP_AND_DUMB")
                        .with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.percentCorrect").value(100))
                .andExpect(jsonPath("$.submission.llmModel").value("CHEAP_AND_DUMB"))
                .andExpect(jsonPath("$.submission.username").value(PLAYER));

        ArgumentCaptor<PuzzleDto> sentToClaude = ArgumentCaptor.forClass(PuzzleDto.class);
        verify(claudeApiClient).solveCrossword(sentToClaude.capture(), eq(ClaudeModel.CHEAP_AND_DUMB));
        sentToClaude.getValue().grid().forEach(row -> assertTrue(row.matches("[.#]+"), "Claude saw letters: " + row));

        mockMvc.perform(get("/api/crosswords/" + id + "/api-calls").with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(jsonPath("$[2].apiName").value("CLAUDE_SOLVE"))
                .andExpect(jsonPath("$[2].inputTokens").value(1000))
                .andExpect(jsonPath("$[2].outputTokens").value(200));
    }

    @Test
    void adminMayUseTheMostExpensiveModel() throws Exception {
        int id = idOf(createCrossword(null));
        when(claudeApiClient.solveCrossword(any(), eq(ClaudeModel.TOTAL_TOKEN_KILLER)))
                .thenReturn(new ClaudeSolution(ANSWER_KEY, null, null));

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions/claude")
                        .param("model", "TOTAL_TOKEN_KILLER")
                        .with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @Test
    void playerSubmissionIsScoredAndCarriesTheUsername() throws Exception {
        int id = idOf(createCrossword(null));
        int attemptId = startAttempt(id, PLAYER);

        // Two wrong letters out of 23 letter cells: 21/23 = 91 %.
        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grid": ["XX#CD", "EFGHI", "JKLMN", "OPQRS", "TU#VW"], "attemptId": %d}
                                """.formatted(attemptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.percentCorrect").value(91))
                .andExpect(jsonPath("$.submission.username").value(PLAYER))
                .andExpect(jsonPath("$.cellMarks[0]").value("--#++"));
    }

    @Test
    void submissionWithWrongGridSizeGives400() throws Exception {
        int id = idOf(createCrossword(null));

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": [\"AB#CD\"], \"attemptId\": " + startAttempt(id, PLAYER) + "}"))
                .andExpect(status().isBadRequest());
    }

    // A time sent by the browser is ignored: the backend measures from when the attempt started.
    @Test
    void playerTimeIsMeasuredFromTheAttemptNotTakenFromTheBrowser() throws Exception {
        int id = idOf(createCrossword(null));
        int attemptId = startAttempt(id, PLAYER);
        Thread.sleep(50);

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": " + objectMapper.writeValueAsString(ANSWER_KEY)
                                + ", \"attemptId\": " + attemptId + ", \"timeToSolveMs\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.timeToSolveMs").value(greaterThanOrEqualTo(50)));
    }

    @Test
    void anotherPlayersAttemptGives403() throws Exception {
        int id = idOf(createCrossword(null));
        int adminsAttempt = startAttempt(id, ADMIN);

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": " + objectMapper.writeValueAsString(ANSWER_KEY)
                                + ", \"attemptId\": " + adminsAttempt + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAttemptForAnotherCrosswordGives404AndNoAttemptGives400() throws Exception {
        int id = idOf(createCrossword(null));
        int otherId = idOf(createCrossword(null));
        int attemptForOther = startAttempt(otherId, PLAYER);
        String grid = objectMapper.writeValueAsString(ANSWER_KEY);

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": " + grid + ", \"attemptId\": " + attemptForOther + "}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/crosswords/" + id + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": " + grid + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerCanShowTheirSubmissionAgainWithTheSameMarks() throws Exception {
        int crosswordId = idOf(createCrossword(null));
        int submissionId = submitAsPlayer(crosswordId);

        mockMvc.perform(get("/api/crosswords/" + crosswordId + "/submissions/" + submissionId)
                        .with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submission.username").value(PLAYER))
                .andExpect(jsonPath("$.submittedGrid[0]").value("AB#CX"))
                .andExpect(jsonPath("$.cellMarks.length()").value(5));
    }

    @Test
    void anotherUserCannotSeeTheSubmissionEvenAsAdmin() throws Exception {
        int crosswordId = idOf(createCrossword(null));
        int submissionId = submitAsPlayer(crosswordId);

        mockMvc.perform(get("/api/crosswords/" + crosswordId + "/submissions/" + submissionId)
                        .with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only see your own submissions"));
    }

    @Test
    void submissionUnderTheWrongCrosswordGives404() throws Exception {
        int crosswordId = idOf(createCrossword(null));
        int otherCrosswordId = idOf(createCrossword(null));
        int submissionId = submitAsPlayer(crosswordId);

        mockMvc.perform(get("/api/crosswords/" + otherCrosswordId + "/submissions/" + submissionId)
                        .with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isNotFound());
    }

    // A full answer with one wrong letter in the first row.
    private int submitAsPlayer(int crosswordId) throws Exception {
        String grid = objectMapper.writeValueAsString(List.of("AB#CX", "EFGHI", "JKLMN", "OPQRS", "TU#VW"));
        String body = mockMvc.perform(post("/api/crosswords/" + crosswordId + "/submissions")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grid\": " + grid + ", \"attemptId\": " + startAttempt(crosswordId, PLAYER) + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("submission").get("id").asInt();
    }

    private int startAttempt(int crosswordId, String username) throws Exception {
        String body = mockMvc.perform(post("/api/crosswords/" + crosswordId + "/attempts")
                        .with(httpBasic(username, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asInt();
    }

    private String createCrossword(String theme) throws Exception {
        String themeJson = theme == null ? "null" : "\"" + theme + "\"";
        return mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(PLAYER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridSize\": 5, \"playerPrompt\": " + themeJson + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private int idOf(String crosswordJson) {
        return objectMapper.readTree(crosswordJson).get("id").asInt();
    }
}
