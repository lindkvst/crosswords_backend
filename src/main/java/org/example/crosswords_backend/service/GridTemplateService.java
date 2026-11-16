package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.GridSizeDto;
import org.example.crosswords_backend.dto.GridTemplateDto;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.model.GridTemplate;
import org.example.crosswords_backend.repository.GridTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class GridTemplateService {

    private final GridTemplateRepository gridTemplateRepository;

    public GridTemplateService(GridTemplateRepository gridTemplateRepository) {
        this.gridTemplateRepository = gridTemplateRepository;
    }

    public List<GridTemplateDto> findAll() {
        return gridTemplateRepository.findAll().stream().map(this::toDto).toList();
    }

    // Sorted by size, so the dropdown lists 5, 7, 9, 13.
    public List<GridSizeDto> findSizes() {
        Map<Integer, Long> layoutsBySize = gridTemplateRepository.findAll().stream()
                .collect(Collectors.groupingBy(GridTemplate::getSize, TreeMap::new, Collectors.counting()));
        return layoutsBySize.entrySet().stream()
                .map(entry -> new GridSizeDto(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }

    // A random layout of the size, so two 5x5 crosswords rarely have the same black cells.
    // There are only a handful of templates, so filtering in Java is simpler than a query on the layout text.
    public GridTemplate findRandomBySize(int size) {
        List<GridTemplate> matching = gridTemplateRepository.findAll().stream()
                .filter(template -> template.getSize() == size)
                .toList();
        if (matching.isEmpty()) {
            throw new ResourceNotFoundException("No grid template with size " + size);
        }
        return matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
    }

    private GridTemplateDto toDto(GridTemplate template) {
        List<String> rows = template.getRows();
        return new GridTemplateDto(template.getId(), template.getName(), rows.size(), rows.get(0).length());
    }
}
