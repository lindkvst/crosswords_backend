package org.example.crosswords_backend.dto;

import java.util.List;

// The role matrix as it is in the database: all permissions (columns) and every role with the permissions it has (rows).
public record RoleMatrixDto(List<PermissionDto> permissions, List<RoleDto> roles) {

    public record PermissionDto(String name, String description) {
    }

    public record RoleDto(String name, String description, List<String> permissions) {
    }
}
