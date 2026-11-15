package org.example.crosswords_backend.dto;

import java.time.Instant;

// The answer to a login. The frontend sends the token as "Authorization: Bearer <token>" until it expires.
public record TokenDto(String token, Instant expiresAt) {
}
