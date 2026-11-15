package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Named AppUser because USER is a reserved word in MySQL.
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;

    // A BCrypt hash. A plain-text password is never stored.
    private String passwordHash;

    // STRING stores "PLAYER"/"ADMIN" instead of 0/1, so reordering the enum cannot corrupt data.
    @Enumerated(EnumType.STRING)
    private Role role;

    protected AppUser() {
        // required by JPA
    }

    public AppUser(String username, String email, String passwordHash, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
