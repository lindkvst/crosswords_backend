package org.example.crosswords_backend.model;

// The three Claude models a player can choose between.
// The frontend only ever sends the name (e.g. CHEAP_AND_DUMB). The backend decides the actual model,
// so nobody can make our backend call an arbitrary model on our bill.
public enum ClaudeModel {
    CHEAP_AND_DUMB("claude-haiku-5-5"),
    SMARTER_AND_PRICIER("claude-sonnet-5-5"),
    TOTAL_TOKEN_KILLER("claude-fable-5-2");

    private final String modelId;

    ClaudeModel(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}
