package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Login with username and password gives a JWT, and the JWT alone is enough for the rest of the API.
class JwtIntegrationTest extends IntegrationTestBase {

    private static final String PLAYER = "jwt-player";
    private static final String ADMIN = "jwt-admin";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setUp() {
        ensureUser(PLAYER, Role.PLAYER);
        ensureUser(ADMIN, Role.ADMIN);
    }

    @Test
    void loginGivesATokenThatWorksWithoutThePassword() throws Exception {
        String token = login(PLAYER);

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(PLAYER))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void loginWithAWrongPasswordGives401() throws Exception {
        mockMvc.perform(post("/api/auth/login").with(httpBasic(emailOf(PLAYER), "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aChangedTokenGives401AsJsonWithoutWwwAuthenticate() throws Exception {
        String token = login(PLAYER);
        // Changing one character in the middle of the signature (the part after the last dot) makes the token invalid.
        int i = token.length() - 10;
        String tampered = token.substring(0, i) + (token.charAt(i) == 'A' ? 'B' : 'A') + token.substring(i + 1);

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login required"))
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void anExpiredTokenGives401() throws Exception {
        Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(PLAYER)
                .issuedAt(twoHoursAgo)
                .expiresAt(twoHoursAgo.plus(1, ChronoUnit.HOURS))
                .claim("roles", List.of("PLAYER"))
                .build();
        String expired = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theRoleInTheTokenDecidesTheAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + login(PLAYER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin only"));
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + login(ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void aTokenCannotBeSwappedForANewToken() throws Exception {
        mockMvc.perform(post("/api/auth/login").header("Authorization", "Bearer " + login(PLAYER)))
                .andExpect(status().isBadRequest());
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login").with(httpBasic(emailOf(username), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
