package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.UserDto;
import org.example.crosswords_backend.dto.UserRegistrationDto;
import org.example.crosswords_backend.exception.InvalidRegistrationException;
import org.example.crosswords_backend.exception.ResourceNotFoundException;
import org.example.crosswords_backend.exception.EmailTakenException;
import org.example.crosswords_backend.exception.UsernameTakenException;
import org.example.crosswords_backend.model.AppUser;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        requireValid(request);
        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        requireUsernameFree(username);
        requireEmailFree(email);
        return toDto(saveNewPlayer(username, email, request.password()));
    }

    // Used both when registering and when logging in, so the same email always finds the same user.
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public List<UserDto> findAll() {
        return appUserRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto findById(int id) {
        return toDto(findEntityById(id));
    }

    // For other services, which need the entity to set it as owner of a crossword or submission.
    public AppUser findEntityById(int id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user with id " + id));
    }

    // Everyone who registers is a PLAYER. An ADMIN can only be made directly in the database.
    // Two registrations with the same email or username at the same moment both pass the checks,
    // so the unique columns stop the second one here. We cannot tell which column it was, hence one message.
    private AppUser saveNewPlayer(String username, String email, String password) {
        AppUser user = new AppUser(username, email, passwordEncoder.encode(password), Role.PLAYER);
        try {
            return appUserRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailTakenException("The email or username is already taken");
        }
    }

    private void requireValid(UserRegistrationDto request) {
        if (request == null || isBlank(request.username()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new InvalidRegistrationException("Username, email and password are required");
        }
        if (!request.email().contains("@")) {
            throw new InvalidRegistrationException("The email is not valid");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new InvalidRegistrationException("The password is too long");
        }
    }

    // Quick checks with a clear message. The unique columns in saveNewPlayer() are the real guarantee.
    private void requireUsernameFree(String username) {
        if (appUserRepository.existsByUsername(username)) {
            throw new UsernameTakenException("The username is already taken");
        }
    }

    private void requireEmailFree(String email) {
        if (appUserRepository.existsByEmail(email)) {
            throw new EmailTakenException("The email is already registered");
        }
    }

    private UserDto toDto(AppUser user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
