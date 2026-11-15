package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.GridTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GridTemplateRepository extends JpaRepository<GridTemplate, Integer> {

    boolean existsByName(String name);
}
