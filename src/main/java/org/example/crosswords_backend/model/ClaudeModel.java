package org.example.crosswords_backend.model;

// The three Claude models a player can choose between.
// The frontend only ever sends the name (e.g. CHEAP_AND_DUMB). The backend decides the actual model,
// so nobody can make our backend call an arbitrary model on our bill.
public enum ClaudeModel {
    CHEAP_AND_DUMB("claude-haiku-5-5", 300, PermissionName.SOLVE_WITH_CLAUDE),
    SMARTER_AND_PRICIER("claude-sonnet-5-5", 700, PermissionName.SOLVE_WITH_CLAUDE),
    // The most expensive model needs its own permission, so a player cannot run up the bill.
    TOTAL_TOKEN_KILLER("claude-fable-5-2", 1400, PermissionName.USE_PREMIUM_MODEL);

    private final String modelId;
    // Our own rough guess at the power the model draws while it works on one request.
    // Anthropic publishes no such numbers, so it is only good for comparing, not as a fact.
    private final int estimatedWatts;
    private final PermissionName requiredPermission;

    ClaudeModel(String modelId, int estimatedWatts, PermissionName requiredPermission) {
        this.modelId = modelId;
        this.estimatedWatts = estimatedWatts;
        this.requiredPermission = requiredPermission;
    }

    public PermissionName getRequiredPermission() {
        return requiredPermission;
    }

    public int getEstimatedWatts() {
        return estimatedWatts;
    }

    public String getModelId() {
        return modelId;
    }
}
