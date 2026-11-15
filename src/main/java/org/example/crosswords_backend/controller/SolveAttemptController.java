package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.SolveAttemptDto;
import org.example.crosswords_backend.service.SolveAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

// The frontend calls this when a crossword is shown, so the clock starts on the server.
@RestController
@RequestMapping("/api/crosswords/{crosswordId}/attempts")
public class SolveAttemptController {

    private final SolveAttemptService solveAttemptService;

    public SolveAttemptController(SolveAttemptService solveAttemptService) {
        this.solveAttemptService = solveAttemptService;
    }

    @PostMapping
    public ResponseEntity<SolveAttemptDto> start(@PathVariable int crosswordId, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solveAttemptService.start(crosswordId, principal.getName()));
    }
}
