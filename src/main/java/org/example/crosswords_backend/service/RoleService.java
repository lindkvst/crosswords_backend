package org.example.crosswords_backend.service;

import org.example.crosswords_backend.dto.RoleMatrixDto;
import org.example.crosswords_backend.model.Permission;
import org.example.crosswords_backend.model.Role;
import org.example.crosswords_backend.repository.PermissionRepository;
import org.example.crosswords_backend.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // Read from the tables, not from the code, so it shows the matrix as it really is (also roles added with SQL).
    public RoleMatrixDto findMatrix() {
        List<RoleMatrixDto.PermissionDto> permissions = permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(p -> new RoleMatrixDto.PermissionDto(p.getName().name(), p.getDescription()))
                .toList();
        List<RoleMatrixDto.RoleDto> roles = roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getName))
                .map(this::toDto)
                .toList();
        return new RoleMatrixDto(permissions, roles);
    }

    private RoleMatrixDto.RoleDto toDto(Role role) {
        List<String> names = role.getPermissions().stream().map(p -> p.getName().name()).sorted().toList();
        return new RoleMatrixDto.RoleDto(role.getName(), role.getDescription(), names);
    }
}
