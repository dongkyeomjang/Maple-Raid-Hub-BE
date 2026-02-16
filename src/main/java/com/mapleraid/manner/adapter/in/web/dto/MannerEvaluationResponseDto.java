package com.mapleraid.manner.adapter.in.web.dto;

import com.mapleraid.manner.application.port.in.output.SubmitMannerEvaluationResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MannerEvaluationResponseDto(
        String id,
        String evaluateeId,
        List<String> tags,
        BigDecimal temperatureChange,
        Instant createdAt
) {
    public static MannerEvaluationResponseDto from(SubmitMannerEvaluationResult result) {
        return new MannerEvaluationResponseDto(
                result.getId(),
                result.getEvaluateeId(),
                result.getTags(),
                result.getTemperatureChange(),
                result.getCreatedAt()
        );
    }
}
