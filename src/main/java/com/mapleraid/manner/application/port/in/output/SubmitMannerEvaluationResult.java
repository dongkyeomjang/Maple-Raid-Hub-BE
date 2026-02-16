package com.mapleraid.manner.application.port.in.output;

import com.mapleraid.manner.domain.MannerEvaluation;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class SubmitMannerEvaluationResult {

    private final String id;
    private final String evaluateeId;
    private final List<String> tags;
    private final BigDecimal temperatureChange;
    private final Instant createdAt;

    private SubmitMannerEvaluationResult(String id, String evaluateeId, List<String> tags,
                                          BigDecimal temperatureChange, Instant createdAt) {
        this.id = id;
        this.evaluateeId = evaluateeId;
        this.tags = tags;
        this.temperatureChange = temperatureChange;
        this.createdAt = createdAt;
    }

    public static SubmitMannerEvaluationResult from(MannerEvaluation evaluation) {
        return new SubmitMannerEvaluationResult(
                evaluation.getId().getValue().toString(),
                evaluation.getEvaluateeId().getValue().toString(),
                evaluation.getTags().stream().map(Enum::name).toList(),
                evaluation.getTemperatureChange(),
                evaluation.getCreatedAt()
        );
    }
}
