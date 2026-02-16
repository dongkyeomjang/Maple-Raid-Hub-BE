package com.mapleraid.manner.application.port.out;

import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.user.domain.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MannerEvaluationRepository {

    MannerEvaluation save(MannerEvaluation evaluation);

    List<MannerEvaluation> findByEvaluateeId(UserId evaluateeId);

    Optional<MannerEvaluation> findLatestByEvaluatorIdAndEvaluateeId(UserId evaluatorId, UserId evaluateeId);

    boolean existsByEvaluatorIdAndEvaluateeIdAndCreatedAtAfter(UserId evaluatorId, UserId evaluateeId, Instant after);
}
