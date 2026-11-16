package org.example.crosswords_backend.controller;

import org.example.crosswords_backend.dto.ApiUsageSummaryDto;
import org.example.crosswords_backend.dto.UserDto;
import org.example.crosswords_backend.dto.RoleMatrixDto;
import org.example.crosswords_backend.service.ApiCallService;
import org.example.crosswords_backend.service.RoleService;
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
    private final RoleService roleService;

    public AdminController(UserService userService, ApiCallService apiCallService, RoleService roleService) {
        this.userService = userService;
        this.apiCallService = apiCallService;
        this.roleService = roleService;
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

    // Which role has which permission, read from the database.
    @GetMapping("/roles")
    public RoleMatrixDto roles() {
        return roleService.findMatrix();
    }
}
