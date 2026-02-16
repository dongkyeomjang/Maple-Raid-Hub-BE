package com.mapleraid.manner.adapter.in.web.dto;

import com.mapleraid.manner.application.port.in.output.CheckEvaluationAvailabilityResult;

import java.time.Instant;

public record EvaluationAvailabilityDto(
        boolean canEvaluate,
        Instant nextAvailableAt
) {
    public static EvaluationAvailabilityDto from(CheckEvaluationAvailabilityResult result) {
        return new EvaluationAvailabilityDto(
                result.isCanEvaluate(),
                result.getNextAvailableAt()
        );
    }
}
