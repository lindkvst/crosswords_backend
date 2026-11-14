package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.CrosswordDto;
import org.example.crosswords_backend.service.CrosswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crosswords")
@CrossOrigin
public class CrosswordController {

    private final CrosswordService crosswordService;

    public CrosswordController(CrosswordService crosswordService) {
        this.crosswordService = crosswordService;
    }

    @PostMapping
    public ResponseEntity<CrosswordDto> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(crosswordService.createCrossword());
    }

    @GetMapping
    public List<CrosswordDto> findAll() {
        return crosswordService.findAll();
    }

    @GetMapping("/{id}")
    public CrosswordDto findById(@PathVariable int id) {
        return crosswordService.findById(id);
    }
}
