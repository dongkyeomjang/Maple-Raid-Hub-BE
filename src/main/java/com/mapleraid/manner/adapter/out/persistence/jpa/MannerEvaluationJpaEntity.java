package com.mapleraid.manner.adapter.out.persistence.jpa;

import com.mapleraid.manner.domain.EvaluationContext;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.manner.domain.MannerEvaluationId;
import com.mapleraid.manner.domain.MannerTag;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "manner_evaluations")
public class MannerEvaluationJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "evaluator_id", nullable = false, length = 36)
    private String evaluatorId;

    @Column(name = "evaluatee_id", nullable = false, length = 36)
    private String evaluateeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "context", nullable = false, length = 20)
    private EvaluationContext context;

    @ElementCollection
    @CollectionTable(name = "manner_evaluation_tags", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "tag")
    @Enumerated(EnumType.STRING)
    private List<MannerTag> tags = new ArrayList<>();

    @Column(name = "temperature_change", precision = 4, scale = 2)
    private BigDecimal temperatureChange;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static MannerEvaluationJpaEntity fromDomain(MannerEvaluation evaluation) {
        MannerEvaluationJpaEntity entity = new MannerEvaluationJpaEntity();
        entity.id = evaluation.getId().getValue().toString();
        entity.evaluatorId = evaluation.getEvaluatorId().getValue().toString();
        entity.evaluateeId = evaluation.getEvaluateeId().getValue().toString();
        entity.context = evaluation.getContext();
        entity.tags = new ArrayList<>(evaluation.getTags());
        entity.temperatureChange = evaluation.getTemperatureChange();
        entity.createdAt = evaluation.getCreatedAt();
        return entity;
    }

    public MannerEvaluation toDomain() {
        return MannerEvaluation.reconstitute(
                MannerEvaluationId.of(id),
                UserId.of(evaluatorId),
                UserId.of(evaluateeId),
                context,
                new ArrayList<>(tags),
                temperatureChange,
                createdAt
        );
    }
}
