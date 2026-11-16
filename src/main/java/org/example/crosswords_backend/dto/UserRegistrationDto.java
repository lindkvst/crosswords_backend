package org.example.crosswords_backend.dto;

// What the frontend sends to register. The password arrives in plain text (over HTTPS in production)
// and is hashed before it is stored.
// username is the display name. email is what the user logs in with.
public record UserRegistrationDto(String username, String email, String password) {
}
