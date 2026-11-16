package org.example.crosswords_backend.dto;

import java.util.List;

// role is the role's name. permissions are what the role allows; the frontend uses them to show or hide
// things (e.g. Stats), the backend checks them again on every request.
public record UserDto(int id, String username, String email, String role, List<String> permissions) {
}
