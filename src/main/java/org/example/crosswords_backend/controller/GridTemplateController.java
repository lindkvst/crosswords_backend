package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.GridTemplateDto;
import org.example.crosswords_backend.service.GridTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grid-templates")
public class GridTemplateController {

    private final GridTemplateService gridTemplateService;

    public GridTemplateController(GridTemplateService gridTemplateService) {
        this.gridTemplateService = gridTemplateService;
    }

    @GetMapping
    public List<GridTemplateDto> findAll() {
        return gridTemplateService.findAll();
    }
}
