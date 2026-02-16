package com.mapleraid.manner.adapter.in.web.dto;

import com.mapleraid.manner.application.port.in.output.GetMyEvaluationsResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MyEvaluationDetailDto(
        String id,
        String context,
        List<String> tags,
        BigDecimal temperatureChange,
        Instant createdAt
) {
    public static MyEvaluationDetailDto from(GetMyEvaluationsResult.EvaluationDetail detail) {
        return new MyEvaluationDetailDto(
                detail.getId(),
                detail.getContext(),
                detail.getTags(),
                detail.getTemperatureChange(),
                detail.getCreatedAt()
        );
    }
}
