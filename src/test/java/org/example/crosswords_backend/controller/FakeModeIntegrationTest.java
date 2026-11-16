package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.client.CrosswordSolver;
import org.example.crosswords_backend.client.CrosswordSource;
import org.example.crosswords_backend.client.FakeCrosswordSolver;
import org.example.crosswords_backend.client.FakeCrosswordSource;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.example.crosswords_backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Dependency Inversion in practice: the same services run against the fakes, chosen only by two properties.
// No mocks here: this is what a developer gets with app.crossword-source=fake and app.crossword-solver=fake.
@SpringBootTest(properties = {"app.crossword-source=fake", "app.crossword-solver=fake"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FakeModeIntegrationTest {

    private static final String EMAIL = "fake-mode@example.com";
    private static final String PASSWORD = "test-password-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CrosswordSource crosswordSource;

    @Autowired
    private CrosswordSolver crosswordSolver;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void thePropertiesPickTheFakes() {
        assertInstanceOf(FakeCrosswordSource.class, crosswordSource);
        assertInstanceOf(FakeCrosswordSolver.class, crosswordSolver);
    }

    @Test
    void aCrosswordCanBeMadeAndSolvedWithoutAnyExternalCall() throws Exception {
        if (!appUserRepository.existsByEmail(EMAIL)) {
            appUserRepository.save(new AppUser("fake-mode", EMAIL, passwordEncoder.encode(PASSWORD),
                    roleRepository.findByName(Role.PLAYER).orElseThrow()));
        }
        String body = mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(EMAIL, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridSize\": 7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puzzle.grid.length()").value(7))
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(body).get("id").asInt();

        mockMvc.perform(post("/api/crosswords/" + id + "/submissions/claude")
                        .param("model", "CHEAP_AND_DUMB")
                        .with(httpBasic(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submission.llmModel").value("CHEAP_AND_DUMB"));
    }
}
