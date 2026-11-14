package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.GridTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GridTemplateRepository extends JpaRepository<GridTemplate, Integer> {

    // TODO V1.0 has a single template. Later versions let the player choose.
    Optional<GridTemplate> findFirstByOrderByIdAsc();
}
