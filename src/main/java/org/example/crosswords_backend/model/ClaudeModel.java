package org.example.crosswords_backend.model;

// The three Claude models a player can choose between.
// The frontend only ever sends the name (e.g. CHEAP_AND_DUMB). The backend decides the actual model,
// so nobody can make our backend call an arbitrary model on our bill.
public enum ClaudeModel {
    CHEAP_AND_DUMB("claude-haiku-5-5", 300, false),
    SMARTER_AND_PRICIER("claude-sonnet-5-5", 700, false),
    // The most expensive model. Only an admin may start it, so a player cannot run up the bill.
    TOTAL_TOKEN_KILLER("claude-fable-5-2", 1400, true);

    private final String modelId;
    // Our own rough guess at the power the model draws while it works on one request.
    // Anthropic publishes no such numbers, so it is only good for comparing, not as a fact.
    private final int estimatedWatts;
    private final boolean adminOnly;

    ClaudeModel(String modelId, int estimatedWatts, boolean adminOnly) {
        this.modelId = modelId;
        this.estimatedWatts = estimatedWatts;
        this.adminOnly = adminOnly;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public int getEstimatedWatts() {
        return estimatedWatts;
    }

    public String getModelId() {
        return modelId;
    }
}
