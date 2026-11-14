package org.example.crosswords_backend.config;

import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitData implements CommandLineRunner {

    private final GridTemplateRepository gridTemplateRepository;

    public InitData(GridTemplateRepository gridTemplateRepository) {
        this.gridTemplateRepository = gridTemplateRepository;
    }

    @Override
    public void run(String... args) {
        // Only on an empty table, so restarting the app does not create duplicates.
        if (gridTemplateRepository.count() == 0) {
            gridTemplateRepository.save(new GridTemplate("mini-5x5", "..#../...../...../...../..#.."));
        }
    }
}
