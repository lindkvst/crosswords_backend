package org.example.crosswords_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

// A role is data: a name and a set of permissions (the role_permission join table). A new role is new rows,
// not new code, and the checks in the code never mention a role, only a PermissionName.
@Entity
public class Role {

    // The two roles that always exist. New users get PLAYER. An ADMIN is made with SQL.
    public static final String PLAYER = "PLAYER";
    public static final String ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Many roles can have the same permission, and a role has many permissions: many-to-many.
    // EAGER, because every login and every check needs the permissions.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
        // required by JPA
    }

    public Role(String name, String description, Set<Permission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
    }

    public boolean hasPermission(PermissionName permission) {
        return permissions.stream().anyMatch(p -> p.getName() == permission);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
