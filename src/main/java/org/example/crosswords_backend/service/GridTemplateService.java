package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.GridTemplateDto;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GridTemplateService {

    private final GridTemplateRepository gridTemplateRepository;

    public GridTemplateService(GridTemplateRepository gridTemplateRepository) {
        this.gridTemplateRepository = gridTemplateRepository;
    }

    public List<GridTemplateDto> findAll() {
        return gridTemplateRepository.findAll().stream().map(this::toDto).toList();
    }

    public GridTemplate findEntityById(int id) {
        return gridTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No grid template with id " + id));
    }

    private GridTemplateDto toDto(GridTemplate template) {
        List<String> rows = template.getRows();
        return new GridTemplateDto(template.getId(), template.getName(), rows.size(), rows.get(0).length());
    }
}
