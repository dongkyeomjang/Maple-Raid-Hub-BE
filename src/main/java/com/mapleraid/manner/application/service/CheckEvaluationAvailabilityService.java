package com.mapleraid.manner.application.service;

import com.mapleraid.manner.application.port.in.input.CheckEvaluationAvailabilityInput;
import com.mapleraid.manner.application.port.in.output.CheckEvaluationAvailabilityResult;
import com.mapleraid.manner.application.port.in.usecase.CheckEvaluationAvailabilityUseCase;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckEvaluationAvailabilityService implements CheckEvaluationAvailabilityUseCase {

    private static final Duration EVALUATION_COOLDOWN = Duration.ofDays(30);

    private final MannerEvaluationRepository mannerEvaluationRepository;

    @Override
    @Transactional(readOnly = true)
    public CheckEvaluationAvailabilityResult execute(CheckEvaluationAvailabilityInput input) {
        // 자기 자신 체크
        if (input.getEvaluatorId().equals(input.getTargetUserId())) {
            return CheckEvaluationAvailabilityResult.unavailable(null);
        }

        // 30일 제한 체크
        Optional<MannerEvaluation> latest = mannerEvaluationRepository
                .findLatestByEvaluatorIdAndEvaluateeId(input.getEvaluatorId(), input.getTargetUserId());

        if (latest.isPresent()) {
            Instant nextAvailable = latest.get().getCreatedAt().plus(EVALUATION_COOLDOWN);
            if (Instant.now().isBefore(nextAvailable)) {
                return CheckEvaluationAvailabilityResult.unavailable(nextAvailable);
            }
        }

        return CheckEvaluationAvailabilityResult.available();
    }
}
