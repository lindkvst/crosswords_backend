package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.TokenDto;
import org.example.crosswords_backend.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // Called with "Authorization: Basic ...". Spring Security has already checked the password (BCrypt)
    // before this method runs, so it only has to issue the token.
    @PostMapping("/login")
    public TokenDto login(Authentication authentication) {
        return tokenService.createToken(authentication);
    }
}
