package com.mapleraid.manner.adapter.out.persistence;

import com.mapleraid.manner.adapter.out.persistence.jpa.MannerEvaluationJpaEntity;
import com.mapleraid.manner.adapter.out.persistence.jpa.MannerEvaluationJpaRepository;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class MannerPersistenceAdapter implements MannerEvaluationRepository {

    private final MannerEvaluationJpaRepository jpaRepository;

    public MannerPersistenceAdapter(MannerEvaluationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MannerEvaluation save(MannerEvaluation evaluation) {
        MannerEvaluationJpaEntity entity = MannerEvaluationJpaEntity.fromDomain(evaluation);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public List<MannerEvaluation> findByEvaluateeId(UserId evaluateeId) {
        return jpaRepository.findByEvaluateeIdOrderByCreatedAtDesc(evaluateeId.getValue().toString()).stream()
                .map(MannerEvaluationJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<MannerEvaluation> findLatestByEvaluatorIdAndEvaluateeId(UserId evaluatorId, UserId evaluateeId) {
        return jpaRepository.findTopByEvaluatorIdAndEvaluateeIdOrderByCreatedAtDesc(
                evaluatorId.getValue().toString(), evaluateeId.getValue().toString())
                .map(MannerEvaluationJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEvaluatorIdAndEvaluateeIdAndCreatedAtAfter(UserId evaluatorId, UserId evaluateeId, Instant after) {
        return jpaRepository.existsByEvaluatorIdAndEvaluateeIdAndCreatedAtAfter(
                evaluatorId.getValue().toString(), evaluateeId.getValue().toString(), after);
    }
}
