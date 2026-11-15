package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.SubmissionDto;
import org.example.crosswords_backend.dto.SubmissionRequestDto;
import org.example.crosswords_backend.dto.SubmissionResultDto;
import org.example.crosswords_backend.model.ClaudeModel;
import org.example.crosswords_backend.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

// A submission always belongs to one crossword, so it lives under that crossword's URL.
@RestController
@RequestMapping("/api/crosswords/{crosswordId}/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResultDto> submit(@PathVariable int crosswordId,
                                                      @RequestBody SubmissionRequestDto request,
                                                      Principal principal) {
        SubmissionResultDto result = submissionService.submitByPlayer(crosswordId, request, CurrentUser.id(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // No request body: Claude gets the puzzle from the database, not from the client.
    // Spring converts ?model=CHEAP_AND_DUMB to the enum. An unknown name never reaches our code.
    @PostMapping("/claude")
    public ResponseEntity<SubmissionResultDto> submitByClaude(@PathVariable int crosswordId,
                                                              @RequestParam ClaudeModel model,
                                                              Principal principal) {
        SubmissionResultDto result = submissionService.submitByClaude(crosswordId, model, CurrentUser.id(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{submissionId}")
    public SubmissionResultDto findOwnSubmission(@PathVariable int crosswordId,
                                                 @PathVariable int submissionId,
                                                 Principal principal) {
        return submissionService.findOwnSubmission(crosswordId, submissionId, CurrentUser.id(principal));
    }

    @GetMapping
    public List<SubmissionDto> findByCrosswordId(@PathVariable int crosswordId) {
        return submissionService.findByCrossword(crosswordId);
    }
}
