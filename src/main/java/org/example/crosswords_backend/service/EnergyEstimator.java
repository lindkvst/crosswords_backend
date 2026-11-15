package org.example.crosswords_backend.service;

import org.example.crosswords_backend.model.ClaudeModel;

// Estimates energy as power x time, the same way for people and machines, so the numbers can be compared.
// All watt figures are assumptions (see the natlog). The real data we store is the time and Claude's tokens.
public final class EnergyEstimator {

    // The human brain uses about 20 W, whether it solves crosswords or not.
    public static final int HUMAN_BRAIN_WATTS = 20;
    // Guess for the share of a texs server that one fill or clue lookup uses.
    public static final int TEXS_WATTS = 50;

    private static final double MS_PER_HOUR = 3_600_000.0;

    private EnergyEstimator() {
    }

    public static double humanWh(int timeToSolveMs) {
        return wattHours(HUMAN_BRAIN_WATTS, timeToSolveMs);
    }

    public static double texsWh(int durationMs) {
        return wattHours(TEXS_WATTS, durationMs);
    }

    public static double claudeWh(ClaudeModel model, int durationMs) {
        return wattHours(model.getEstimatedWatts(), durationMs);
    }

    private static double wattHours(int watts, int durationMs) {
        return watts * durationMs / MS_PER_HOUR;
    }
}
