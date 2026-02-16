package com.mapleraid.manner.domain;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.user.domain.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class MannerEvaluation {

    private static final int MAX_TAGS = 3;

    private final MannerEvaluationId id;
    private final UserId evaluatorId;
    private final UserId evaluateeId;
    private final EvaluationContext context;
    private final List<MannerTag> tags;
    private BigDecimal temperatureChange;
    private final Instant createdAt;

    private MannerEvaluation(MannerEvaluationId id, UserId evaluatorId, UserId evaluateeId,
                             EvaluationContext context, List<MannerTag> tags,
                             BigDecimal temperatureChange, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.evaluatorId = Objects.requireNonNull(evaluatorId);
        this.evaluateeId = Objects.requireNonNull(evaluateeId);
        this.context = Objects.requireNonNull(context);
        this.tags = List.copyOf(tags);
        this.temperatureChange = temperatureChange;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static MannerEvaluation create(UserId evaluatorId, UserId evaluateeId,
                                           EvaluationContext context, List<MannerTag> tags) {
        if (evaluatorId.equals(evaluateeId)) {
            throw new CommonException(ErrorCode.MANNER_SELF_EVALUATION);
        }

        if (tags == null || tags.isEmpty()) {
            throw new CommonException(ErrorCode.MANNER_NO_TAGS);
        }
        if (tags.size() > MAX_TAGS) {
            throw new CommonException(ErrorCode.MANNER_TOO_MANY_TAGS);
        }

        boolean hasPositive = tags.stream().anyMatch(MannerTag::isPositive);
        boolean hasNegative = tags.stream().anyMatch(MannerTag::isNegative);
        if (hasPositive && hasNegative) {
            throw new CommonException(ErrorCode.MANNER_MIXED_TAGS);
        }

        return new MannerEvaluation(MannerEvaluationId.generate(), evaluatorId, evaluateeId, context, tags, BigDecimal.ZERO, null);
    }

    public static MannerEvaluation reconstitute(
            MannerEvaluationId id, UserId evaluatorId, UserId evaluateeId,
            EvaluationContext context, List<MannerTag> tags,
            BigDecimal temperatureChange, Instant createdAt) {
        return new MannerEvaluation(id, evaluatorId, evaluateeId, context, tags, temperatureChange, createdAt);
    }

    /**
     * 서비스에서 실제 온도 변화량을 계산한 뒤 설정
     */
    public void applyTemperatureChange(BigDecimal actualChange) {
        this.temperatureChange = actualChange;
    }

    public MannerEvaluationId getId() { return id; }
    public UserId getEvaluatorId() { return evaluatorId; }
    public UserId getEvaluateeId() { return evaluateeId; }
    public EvaluationContext getContext() { return context; }
    public List<MannerTag> getTags() { return tags; }
    public BigDecimal getTemperatureChange() { return temperatureChange; }
    public Instant getCreatedAt() { return createdAt; }
}
