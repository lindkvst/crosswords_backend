package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.model.Crossword;
import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.CrosswordRepository;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends IntegrationTestBase {

    private static final String PLAYER = "sec-player";
    private static final String ADMIN = "sec-admin";
    private static final String FRONTEND = "http://localhost:63342";

    @Autowired
    private CrosswordRepository crosswordRepository;

    @Autowired
    private GridTemplateRepository gridTemplateRepository;

    @BeforeEach
    void createUsers() {
        ensureUser(PLAYER, Role.PLAYER);
        ensureUser(ADMIN, Role.ADMIN);
    }

    @Test
    void registerCreatesPlayerAndNeverReturnsTheHash() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "new-player", "email": "n@example.com", "password": "secret-123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new-player"))
                .andExpect(jsonPath("$.role").value("PLAYER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registeringATakenUsernameGives409() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + PLAYER + "\", \"email\": \"x@example.com\", \"password\": \"abc\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registeringWithABlankFieldGives400() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \" \", \"email\": \"x@example.com\", \"password\": \"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withoutLoginTheAnswerIs401AsJsonWithoutBrowserPopup() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void wrongPasswordGives401() throws Exception {
        mockMvc.perform(get("/api/users/me").with(httpBasic(PLAYER, "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loggedInUserGetsThemselvesAndNoSessionCookie() throws Exception {
        mockMvc.perform(get("/api/users/me").with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(PLAYER))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void creatingACrosswordRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/crosswords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridTemplateId\": 1}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(crosswordApiClient);
    }

    @Test
    void preflightFromTheFrontendGetsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/crosswords")
                        .header("Origin", FRONTEND)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND));
    }

    @Test
    void preflightFromAnotherOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/crosswords")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void playerCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403));
    }

    @Test
    void adminCanSeeUsersAndUsage() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == '" + ADMIN + "')].role").value("ADMIN"));
        mockMvc.perform(get("/api/admin/usage").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void playerCannotStartTheMostExpensiveModelAndClaudeIsNeverCalled() throws Exception {
        GridTemplate template = gridTemplateRepository.findAll().get(0);
        Crossword crossword = crosswordRepository.save(
                new Crossword(template, null, null, "{\"grid\":[],\"clues\":[]}", "[]"));

        mockMvc.perform(post("/api/crosswords/" + crossword.getId() + "/submissions/claude")
                        .param("model", "TOTAL_TOKEN_KILLER")
                        .with(httpBasic(PLAYER, PASSWORD)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(claudeApiClient);
    }
}
