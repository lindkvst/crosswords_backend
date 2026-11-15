package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.UserDto;
import org.example.crosswords_backend.dto.UserRegistrationDto;
import org.example.crosswords_backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegistrationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    // Principal is the user Spring Security authenticated from the Basic header, never a value from the body.
    @GetMapping("/me")
    public UserDto me(Principal principal) {
        return userService.findByUsername(principal.getName());
    }
}
