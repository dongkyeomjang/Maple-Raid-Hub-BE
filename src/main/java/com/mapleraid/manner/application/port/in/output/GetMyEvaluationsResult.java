package com.mapleraid.manner.application.port.in.output;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class GetMyEvaluationsResult {

    private final List<EvaluationDetail> evaluations;

    public GetMyEvaluationsResult(List<EvaluationDetail> evaluations) {
        this.evaluations = evaluations;
    }

    @Getter
    public static class EvaluationDetail {
        private final String id;
        private final String context;
        private final List<String> tags;
        private final BigDecimal temperatureChange;
        private final Instant createdAt;

        public EvaluationDetail(String id, String context,
                                List<String> tags, BigDecimal temperatureChange, Instant createdAt) {
            this.id = id;
            this.context = context;
            this.tags = tags;
            this.temperatureChange = temperatureChange;
            this.createdAt = createdAt;
        }
    }
}
