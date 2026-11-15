package org.example.crosswords_backend.model;

// The catalogue of everything that needs a permission. The code always checks one of these, never a role name,
// so a new role is only new rows in the role and role_permission tables (see InitData and the role matrix in Notion).
// A new permission is new code anyway, because something has to check it.
public enum PermissionName {
    CREATE_CROSSWORD("Lave nye krydsord (bruger texs-kvoten)"),
    SOLVE_WITH_CLAUDE("Lade Claude løse med de almindelige modeller"),
    USE_PREMIUM_MODEL("Bruge den dyreste model (TOTAL_TOKEN_KILLER)"),
    VIEW_STATS("Se brugerliste, samlet forbrug og rollematrix"),
    UNLIMITED_USAGE("Ingen daglig grænse på krydsord og Claude-kald");

    private final String description;

    PermissionName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
