package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Client mistakes get the right 4xx, and an unexpected error never shows its internal message to the client.
class ErrorHandlingIntegrationTest extends IntegrationTestBase {

    private static final String PLAYER = "errors-player";

    @BeforeEach
    void setUp() {
        ensureUser(PLAYER, Role.PLAYER);
    }

    @Test
    void anUnknownPathGives404() throws Exception {
        mockMvc.perform(get("/api/nothing-here").with(httpBasic(emailOf(PLAYER), PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No such endpoint"));
    }

    @Test
    void aBodyThatIsNotJsonGives415() throws Exception {
        mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(emailOf(PLAYER), PASSWORD))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("gridSize=5"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void aWrongMethodGives405() throws Exception {
        mockMvc.perform(put("/api/crosswords").with(httpBasic(emailOf(PLAYER), PASSWORD)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void anUnexpectedErrorHidesItsMessage() throws Exception {
        when(crosswordApiClient.fill(anyList()))
                .thenThrow(new IllegalStateException("Table 'app_user' column 'password_hash' exploded"));

        mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(emailOf(PLAYER), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridSize\": 5}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(content().string(not(containsString("password_hash"))));
    }
}
