package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
