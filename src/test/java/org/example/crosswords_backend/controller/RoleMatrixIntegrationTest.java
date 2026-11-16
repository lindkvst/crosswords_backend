package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.PermissionName;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Roles are data: the matrix comes from the tables, and a role that exists only as rows works without new code.
class RoleMatrixIntegrationTest extends IntegrationTestBase {

    private static final String PLAYER = "matrix-player";
    private static final String ADMIN = "matrix-admin";
    private static final String TEACHER = "matrix-teacher";

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        ensureUser(PLAYER, Role.PLAYER);
        ensureUser(ADMIN, Role.ADMIN);
        ensureTeacher();
    }

    @Test
    void meShowsTheRoleAndItsPermissions() throws Exception {
        mockMvc.perform(get("/api/users/me").with(httpBasic(emailOf(PLAYER), PASSWORD)))
                .andExpect(jsonPath("$.role").value(Role.PLAYER))
                .andExpect(jsonPath("$.permissions", contains("CREATE_CROSSWORD", "SOLVE_WITH_CLAUDE")));
    }

    @Test
    void theAdminCanReadTheMatrixFromTheDatabase() throws Exception {
        mockMvc.perform(get("/api/admin/roles").with(httpBasic(emailOf(ADMIN), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(PermissionName.values().length))
                .andExpect(jsonPath("$.roles[?(@.name == 'PLAYER')].permissions[*]")
                        .value(contains("CREATE_CROSSWORD", "SOLVE_WITH_CLAUDE")));
    }

    // TEACHER is not in the code at all: only a row in role and role_permission. It may see stats, but not make
    // crosswords or use Claude, and every check follows from its permissions.
    @Test
    void aRoleThatIsOnlyDataWorksWithoutNewCode() throws Exception {
        mockMvc.perform(get("/api/admin/usage").with(httpBasic(emailOf(TEACHER), PASSWORD)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/crosswords")
                        .with(httpBasic(emailOf(TEACHER), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridSize\": 5}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/crosswords/1/submissions/claude")
                        .param("model", "CHEAP_AND_DUMB")
                        .with(httpBasic(emailOf(TEACHER), PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aPlayerMayNotSeeStats() throws Exception {
        mockMvc.perform(get("/api/admin/roles").with(httpBasic(emailOf(PLAYER), PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Your role does not have permission for this"));
    }

    private void ensureTeacher() {
        Role teacher = roleRepository.findByName("TEACHER").orElseGet(() -> roleRepository.save(
                new Role("TEACHER", "Underviser: ser statistik", Set.of(
                        permissionRepository.findByName(PermissionName.VIEW_STATS).orElseThrow()))));
        if (!appUserRepository.existsByEmail(emailOf(TEACHER))) {
            appUserRepository.save(new AppUser(TEACHER, emailOf(TEACHER), passwordEncoder.encode(PASSWORD), teacher));
        }
    }
}
