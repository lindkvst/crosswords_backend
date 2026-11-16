package org.example.crosswords_backend.config;

import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.model.Permission;
import org.example.crosswords_backend.model.PermissionName;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.example.crosswords_backend.repository.PermissionRepository;
import org.example.crosswords_backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InitData implements CommandLineRunner {

    // Rows separated by '/', '.' to fill and '#' black. Every layout is symmetric when turned 180 degrees,
    // and every white cell is part of a word of at least 3 letters (checked with GridWordFinder).
    // 7x7 and up have no runs of 2 letters, which would look like words but get no clue.
    // Several layouts per size, so the black cells differ from crossword to crossword. Most variants are an
    // existing layout mirrored or turned on its side (transposed), which keeps the symmetry and the word lengths.
    // A method that builds new objects on every call, not a static list of entities: save() sets the id on the
    // object it saves, so a shared list would be "already saved" the second time (found by the tests, where
    // several Spring contexts run in the same JVM). Package-private so InitDataTemplatesTest can check every layout.
    static List<GridTemplate> templates() {
        return List.of(
                new GridTemplate("mini-5x5", "..#../...../...../...../..#.."),
                new GridTemplate("mini-5x5-b", "...../...../#...#/...../....."),
                new GridTemplate("mini-5x5-c", "#..../...../...../...../....#"),
                new GridTemplate("mini-5x5-d", "....#/...../...../...../#...."),
                new GridTemplate("mini-5x5-e", "##.../#..../...../....#/...##"),
                new GridTemplate("mini-5x5-f", "...##/....#/...../#..../##..."),
                new GridTemplate("small-7x7", "...#.../...#.../......./##...##/......./...#.../...#..."),
                new GridTemplate("small-7x7-b", "#.....#/....###/......#/......./#....../###..../#.....#"),
                new GridTemplate("small-7x7-c", "#.....#/.#...../.#...../......./.....#./.....#./#.....#"),
                new GridTemplate("medium-9x9",
                        ".....####/.....#.../.....#.../###....../....#..../......###/...#...../...#...../####....."),
                new GridTemplate("medium-9x9-b",
                        "####...../...#...../...#...../......###/....#..../###....../.....#.../.....#.../.....####"),
                new GridTemplate("medium-9x9-c",
                        "...#....#/...#....#/...#....#/......###/....#..../###....../#....#.../#....#.../#....#..."),
                new GridTemplate("medium-9x9-d",
                        "#....#.../#....#.../#....#.../###....../....#..../......###/...#....#/...#....#/...#....#"),
                new GridTemplate("large-13x13",
                        "...#...##..../...#...#...../...#...#...../.....##....../......#....../....#......##/"
                                + "####.....####/##......#..../......#....../......##...../.....#...#.../"
                                + ".....#...#.../....##...#..."),
                new GridTemplate("large-13x13-b",
                        "....##...#.../.....#...#.../.....#...#.../......##...../......#....../##......#..../"
                                + "####.....####/....#......##/......#....../.....##....../...#...#...../"
                                + "...#...#...../...#...##...."),
                new GridTemplate("large-13x13-c",
                        "......##...../......##...../......#....../###...#....../.....#......#/...#......###/"
                                + "...##...##.../###......#.../#......#...../......#...###/......#....../"
                                + ".....##....../.....##......"),
                new GridTemplate("large-13x13-d",
                        ".....##....../.....##....../......#....../......#...###/#......#...../###......#.../"
                                + "...##...##.../...#......###/.....#......#/###...#....../......#....../"
                                + "......##...../......##....."));
    }

    // The role matrix: which permissions each role starts with. The same table is in Notion as the shared reference.
    // Only the starting values: a role that already exists in the database is left as it is.
    static Map<String, Set<PermissionName>> roleMatrix() {
        return Map.of(
                Role.PLAYER, EnumSet.of(PermissionName.CREATE_CROSSWORD, PermissionName.SOLVE_WITH_CLAUDE),
                Role.ADMIN, EnumSet.allOf(PermissionName.class));
    }

    private static final Map<String, String> ROLE_DESCRIPTIONS = Map.of(
            Role.PLAYER, "Spiller: laver og løser krydsord, kan bede Claude om hjælp inden for den daglige grænse",
            Role.ADMIN, "Administrator: alle rettigheder");

    private final GridTemplateRepository gridTemplateRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public InitData(GridTemplateRepository gridTemplateRepository,
                    PermissionRepository permissionRepository,
                    RoleRepository roleRepository) {
        this.gridTemplateRepository = gridTemplateRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        seedTemplates();
        seedPermissions();
        seedRoles();
    }

    // Adds only the templates that are missing, matched by name. Existing rows are never changed,
    // because saved crosswords point to them.
    private void seedTemplates() {
        for (GridTemplate template : templates()) {
            if (!gridTemplateRepository.existsByName(template.getName())) {
                gridTemplateRepository.save(template);
            }
        }
    }

    // One row per entry in the PermissionName catalogue.
    private void seedPermissions() {
        for (PermissionName name : PermissionName.values()) {
            if (permissionRepository.findByName(name).isEmpty()) {
                permissionRepository.save(new Permission(name));
            }
        }
    }

    private void seedRoles() {
        roleMatrix().forEach((roleName, permissionNames) -> {
            if (!roleRepository.existsByName(roleName)) {
                Set<Permission> permissions = permissionNames.stream()
                        .map(name -> permissionRepository.findByName(name).orElseThrow())
                        .collect(Collectors.toSet());
                roleRepository.save(new Role(roleName, ROLE_DESCRIPTIONS.get(roleName), permissions));
            }
        });
    }
}
