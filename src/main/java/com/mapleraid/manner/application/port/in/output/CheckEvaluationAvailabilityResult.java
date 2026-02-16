package com.mapleraid.manner.application.port.in.output;

import lombok.Getter;

import java.time.Instant;

@Getter
public class CheckEvaluationAvailabilityResult {

    private final boolean canEvaluate;
    private final Instant nextAvailableAt;

    private CheckEvaluationAvailabilityResult(boolean canEvaluate, Instant nextAvailableAt) {
        this.canEvaluate = canEvaluate;
        this.nextAvailableAt = nextAvailableAt;
    }

    public static CheckEvaluationAvailabilityResult available() {
        return new CheckEvaluationAvailabilityResult(true, null);
    }

    public static CheckEvaluationAvailabilityResult unavailable(Instant nextAvailableAt) {
        return new CheckEvaluationAvailabilityResult(false, nextAvailableAt);
    }
}
