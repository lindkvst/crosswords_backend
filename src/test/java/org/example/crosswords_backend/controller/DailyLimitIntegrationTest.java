package org.example.crosswords_backend.controller;

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
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Low limits and the fake source and solver, so the limit itself is what is tested, without mocks.
@SpringBootTest(properties = {
        "app.crossword-source=fake", "app.crossword-solver=fake",
        "app.limits.crosswords-per-day=2", "app.limits.claude-solves-per-day=1"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyLimitIntegrationTest {

    private static final String PASSWORD = "test-password-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void aPlayerGets429AfterTheDailyNumberOfCrosswords() throws Exception {
        String email = user("limit-crosswords", Role.PLAYER);
        createCrossword(email).andExpect(status().isCreated());
        createCrossword(email).andExpect(status().isCreated());

        createCrossword(email)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Daily limit reached: 2 new crosswords per day"));
    }

    @Test
    void aPlayerGets429AfterTheDailyNumberOfClaudeSolves() throws Exception {
        String email = user("limit-claude", Role.PLAYER);
        int id = idOf(createCrossword(email));
        solveWithClaude(email, id).andExpect(status().isCreated());

        solveWithClaude(email, id).andExpect(status().isTooManyRequests());
    }

    @Test
    void theAdminHasNoLimit() throws Exception {
        String email = user("limit-admin", Role.ADMIN);
        for (int i = 0; i < 3; i++) {
            createCrossword(email).andExpect(status().isCreated());
        }
    }

    private String user(String username, String roleName) {
        String email = username + "@example.com";
        if (!appUserRepository.existsByEmail(email)) {
            appUserRepository.save(new AppUser(username, email, passwordEncoder.encode(PASSWORD),
                    roleRepository.findByName(roleName).orElseThrow()));
        }
        return email;
    }

    private ResultActions createCrossword(String email) throws Exception {
        return mockMvc.perform(post("/api/crosswords")
                .with(httpBasic(email, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gridSize\": 5}"));
    }

    private ResultActions solveWithClaude(String email, int crosswordId) throws Exception {
        return mockMvc.perform(post("/api/crosswords/" + crosswordId + "/submissions/claude")
                .param("model", "CHEAP_AND_DUMB")
                .with(httpBasic(email, PASSWORD)));
    }

    private int idOf(ResultActions created) throws Exception {
        return objectMapper.readTree(created.andReturn().getResponse().getContentAsString()).get("id").asInt();
    }
}
