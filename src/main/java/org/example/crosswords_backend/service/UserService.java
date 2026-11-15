package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.UserDto;
import org.example.crosswords_backend.dto.UserRegistrationDto;
import org.example.crosswords_backend.exception.InvalidRegistrationException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.exception.UsernameTakenException;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    // BCrypt only uses the first 72 bytes, and Spring's encoder rejects longer passwords.
    private static final int MAX_PASSWORD_BYTES = 72;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto register(UserRegistrationDto request) {
        if (request == null || isBlank(request.username()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new InvalidRegistrationException("Username, email and password are required");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new InvalidRegistrationException("The password is too long");
        }
        String username = request.username().trim();
        if (appUserRepository.existsByUsername(username)) {
            throw new UsernameTakenException("The username is already taken");
        }

        // Everyone who registers is a PLAYER. An ADMIN can only be made directly in the database.
        AppUser user = new AppUser(username, request.email().trim(),
                passwordEncoder.encode(request.password()), Role.PLAYER);
        try {
            return toDto(appUserRepository.save(user));
        } catch (DataIntegrityViolationException ex) {
            // Two registrations with the same name at the same moment: the unique column stops the second one.
            throw new UsernameTakenException("The username is already taken");
        }
    }

    public UserDto findByUsername(String username) {
        return toDto(findEntityByUsername(username));
    }

    // For other services, which need the entity to set it as owner of a crossword or submission.
    public AppUser findEntityByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No user named " + username));
    }

    private UserDto toDto(AppUser user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
