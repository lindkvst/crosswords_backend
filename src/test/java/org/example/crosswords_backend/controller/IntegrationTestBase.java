package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.client.ClaudeApiClient;
import org.example.crosswords_backend.client.CrosswordApiClient;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// The whole app with H2 instead of MySQL, and the two external API clients replaced by mocks:
// the tests never call texs or Anthropic. All integration tests share this setup,
// so Spring can reuse one application context for them.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    static final String PASSWORD = "test-password-1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    CrosswordApiClient crosswordApiClient;

    @MockitoBean
    ClaudeApiClient claudeApiClient;

    // Saved straight to the database, because the API can only create players.
    void ensureUser(String username, Role role) {
        if (!appUserRepository.existsByUsername(username)) {
            appUserRepository.save(new AppUser(username, username + "@example.com",
                    passwordEncoder.encode(PASSWORD), role));
        }
    }
}
