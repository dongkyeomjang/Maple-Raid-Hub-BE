package com.mapleraid.manner.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MannerEvaluationJpaRepository extends JpaRepository<MannerEvaluationJpaEntity, String> {

    List<MannerEvaluationJpaEntity> findByEvaluateeIdOrderByCreatedAtDesc(String evaluateeId);

    boolean existsByEvaluatorIdAndEvaluateeIdAndCreatedAtAfter(String evaluatorId, String evaluateeId, Instant after);

    Optional<MannerEvaluationJpaEntity> findTopByEvaluatorIdAndEvaluateeIdOrderByCreatedAtDesc(String evaluatorId, String evaluateeId);
}
