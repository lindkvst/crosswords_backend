package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// One row per PermissionName, so roles can point to permissions with a foreign key (role_permission).
@Entity
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // varchar instead of MySQL's enum type, so a new permission needs no ALTER TABLE.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, columnDefinition = "varchar(64)")
    private PermissionName name;

    private String description;

    protected Permission() {
        // required by JPA
    }

    public Permission(PermissionName name) {
        this.name = name;
        this.description = name.getDescription();
    }

    public int getId() {
        return id;
    }

    public PermissionName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
