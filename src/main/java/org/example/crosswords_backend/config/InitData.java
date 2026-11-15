package org.example.crosswords_backend.config;

import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitData implements CommandLineRunner {

    // Rows separated by '/', '.' to fill and '#' black. Every layout is symmetric when turned 180 degrees,
    // and every white cell is part of a word of at least 3 letters (checked with GridWordFinder).
    // 7x7 and up have no runs of 2 letters, which would look like words but get no clue.
    private static final List<GridTemplate> TEMPLATES = List.of(
            new GridTemplate("mini-5x5", "..#../...../...../...../..#.."),
            new GridTemplate("small-7x7", "...#.../...#.../......./##...##/......./...#.../...#..."),
            new GridTemplate("medium-9x9",
                    ".....####/.....#.../.....#.../###....../....#..../......###/...#...../...#...../####....."),
            new GridTemplate("large-13x13",
                    "...#...##..../...#...#...../...#...#...../.....##....../......#....../....#......##/"
                            + "####.....####/##......#..../......#....../......##...../.....#...#.../"
                            + ".....#...#.../....##...#..."));

    private final GridTemplateRepository gridTemplateRepository;

    public InitData(GridTemplateRepository gridTemplateRepository) {
        this.gridTemplateRepository = gridTemplateRepository;
    }

    @Override
    public void run(String... args) {
        // Adds only the templates that are missing, matched by name. Existing rows are never changed,
        // because saved crosswords point to them.
        for (GridTemplate template : TEMPLATES) {
            if (!gridTemplateRepository.existsByName(template.getName())) {
                gridTemplateRepository.save(template);
            }
        }
    }
}
