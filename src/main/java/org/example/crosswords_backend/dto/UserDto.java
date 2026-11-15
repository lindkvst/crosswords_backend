package org.example.crosswords_backend.dto;

import org.example.crosswords_backend.model.Role;

// Deliberately has no password field, so the hash can never be sent to a client.
public record UserDto(int id, String username, String email, Role role) {
}
