package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.ApiUsageSummaryDto;
import org.example.crosswords_backend.dto.UserDto;
import org.example.crosswords_backend.service.ApiCallService;
import org.example.crosswords_backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Everything under /api/admin requires ROLE_ADMIN. The rule lives in SecurityConfig, so no method here can forget it.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ApiCallService apiCallService;

    public AdminController(UserService userService, ApiCallService apiCallService) {
        this.userService = userService;
        this.apiCallService = apiCallService;
    }

    @GetMapping("/users")
    public List<UserDto> findAllUsers() {
        return userService.findAll();
    }

    // The whole bill at a glance: calls, time, tokens and estimated energy per external API.
    @GetMapping("/usage")
    public List<ApiUsageSummaryDto> usage() {
        return apiCallService.summarizeByApi();
    }
}
