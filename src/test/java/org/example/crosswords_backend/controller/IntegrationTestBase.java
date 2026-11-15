package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.client.CrosswordSolver;
import org.example.crosswords_backend.client.CrosswordSource;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.example.crosswords_backend.repository.RoleRepository;
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

    @Autowired
    RoleRepository roleRepository;

    @MockitoBean
    CrosswordSource crosswordApiClient;

    @MockitoBean
    CrosswordSolver claudeApiClient;

    // Users log in with their email. In the tests it is made from the username.
    static String emailOf(String username) {
        return username + "@example.com";
    }

    // Saved straight to the database, because the API can only create players. roleName is Role.PLAYER or Role.ADMIN;
    // the roles themselves come from the role matrix that InitData puts in the (H2) database at startup.
    void ensureUser(String username, String roleName) {
        if (!appUserRepository.existsByEmail(emailOf(username))) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            appUserRepository.save(new AppUser(username, emailOf(username), passwordEncoder.encode(PASSWORD), role));
        }
    }
}
