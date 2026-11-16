package org.example.crosswords_backend.service;

import org.example.crosswords_backend.model.ClaudeModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyEstimatorTest {

    private static final int ONE_HOUR_MS = 3_600_000;

    @Test
    void humanBrainForOneHourIsItsWattage() {
        assertEquals(EnergyEstimator.HUMAN_BRAIN_WATTS, EnergyEstimator.humanWh(ONE_HOUR_MS), 1e-9);
    }

    @Test
    void claudeUsesTheWattageOfTheChosenModel() {
        ClaudeModel model = ClaudeModel.SMARTER_AND_PRICIER;
        assertEquals(model.getEstimatedWatts() / 2.0, EnergyEstimator.claudeWh(model, ONE_HOUR_MS / 2), 1e-9);
    }

    @Test
    void zeroTimeIsZeroEnergy() {
        assertEquals(0.0, EnergyEstimator.texsWh(0), 1e-9);
    }
}
