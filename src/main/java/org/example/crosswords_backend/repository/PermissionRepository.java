package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.Permission;
import org.example.crosswords_backend.model.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByName(PermissionName name);
}
