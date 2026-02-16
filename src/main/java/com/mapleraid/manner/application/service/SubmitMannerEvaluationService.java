package com.mapleraid.manner.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.manner.application.port.in.input.SubmitMannerEvaluationInput;
import com.mapleraid.manner.application.port.in.output.SubmitMannerEvaluationResult;
import com.mapleraid.manner.application.port.in.usecase.SubmitMannerEvaluationUseCase;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.manner.domain.MannerTag;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmitMannerEvaluationService implements SubmitMannerEvaluationUseCase {

    private static final Duration EVALUATION_COOLDOWN = Duration.ofDays(30);
    private static final BigDecimal BASE_TEMPERATURE = new BigDecimal("36.5");
    private static final BigDecimal MAX_UP = new BigDecimal("62.5");   // 36.5 + 62.5 = 99
    private static final BigDecimal MAX_DOWN = new BigDecimal("36.5"); // 36.5 - 36.5 = 0
    private static final double CONFIDENCE_K = 35.0;          // Hill 함수 반포화점 (35회 평가 시 신뢰도 50%)
    private static final double HILL_COEFFICIENT = 2.0;       // Hill 계수 (S-커브: 초반 완만, 중반 급등, 후반 수렴)
    private static final double POSITIVE_EVAL_WEIGHT = 1.0;   // 긍정 평가 1회 가중치
    private static final double NEGATIVE_EVAL_WEIGHT = 1.5;   // 부정 평가 1회 가중치 (더 무겁게)

    private final MannerEvaluationRepository mannerEvaluationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubmitMannerEvaluationResult execute(SubmitMannerEvaluationInput input) {
        // 30일 제한 체크
        Instant thirtyDaysAgo = Instant.now().minus(EVALUATION_COOLDOWN);
        if (mannerEvaluationRepository.existsByEvaluatorIdAndEvaluateeIdAndCreatedAtAfter(
                input.getEvaluatorId(), input.getEvaluateeId(), thirtyDaysAgo)) {
            throw new CommonException(ErrorCode.MANNER_MONTHLY_LIMIT);
        }

        // 대상 사용자 존재 확인
        User evaluatee = userRepository.findById(input.getEvaluateeId())
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        // 기존 평가 이력으로 현재 온도 계산
        List<MannerEvaluation> existingEvaluations = mannerEvaluationRepository.findByEvaluateeId(input.getEvaluateeId());
        BigDecimal oldTemperature = calculateTemperature(existingEvaluations);

        // 매너 평가 생성
        MannerEvaluation evaluation = MannerEvaluation.create(
                input.getEvaluatorId(),
                input.getEvaluateeId(),
                input.getContext(),
                input.getTags()
        );

        // 새 평가 포함하여 온도 재계산
        List<MannerEvaluation> allEvaluations = new ArrayList<>(existingEvaluations);
        allEvaluations.add(evaluation);
        BigDecimal newTemperature = calculateTemperature(allEvaluations);

        // 실제 온도 변화량을 평가에 기록
        BigDecimal actualDelta = newTemperature.subtract(oldTemperature);
        evaluation.applyTemperatureChange(actualDelta);

        MannerEvaluation saved = mannerEvaluationRepository.save(evaluation);

        // 사용자 온도 갱신
        evaluatee.setTemperature(newTemperature);
        userRepository.save(evaluatee);

        return SubmitMannerEvaluationResult.from(saved);
    }

    /**
     * 비율 + Hill 함수 신뢰도 기반 온도 계산 (S-커브).
     *
     * positiveRatio = 긍정가중치합 / (긍정가중치합 + 부정가중치합)
     * confidence = n^h / (K^h + n^h)            // Hill 함수 (h=2, K=35)
     * sentiment = (positiveRatio - 0.5) * 2     // -1.0 ~ +1.0
     *
     * sentiment >= 0: 온도 = 36.5 + sentiment × 62.5 × confidence  (최대 99)
     * sentiment <  0: 온도 = 36.5 + sentiment × 36.5 × confidence  (최저 0)
     *
     * S-커브 특성: 초반 평가는 미미한 변화(+0.05°C), 평가가 쌓일수록 변화폭 증가(피크 ~1.2°C),
     * 이후 점차 수렴. 물의 비열처럼 급격한 온도 변화를 방지.
     */
    static BigDecimal calculateTemperature(List<MannerEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return BASE_TEMPERATURE;
        }

        double totalPositiveWeight = 0;
        double totalNegativeWeight = 0;
        int evaluationCount = evaluations.size();

        // 태그 개수와 무관하게 1회 평가 = 1회 가중치
        for (MannerEvaluation eval : evaluations) {
            boolean isPositive = eval.getTags().stream().anyMatch(MannerTag::isPositive);
            if (isPositive) {
                totalPositiveWeight += POSITIVE_EVAL_WEIGHT;
            } else {
                totalNegativeWeight += NEGATIVE_EVAL_WEIGHT;
            }
        }

        double totalWeight = totalPositiveWeight + totalNegativeWeight;
        if (totalWeight == 0) {
            return BASE_TEMPERATURE;
        }

        double positiveRatio = totalPositiveWeight / totalWeight;
        double sentiment = (positiveRatio - 0.5) * 2.0; // -1.0 ~ +1.0
        double nH = Math.pow(evaluationCount, HILL_COEFFICIENT);
        double kH = Math.pow(CONFIDENCE_K, HILL_COEFFICIENT);
        double confidence = nH / (kH + nH);

        double deviation;
        if (sentiment >= 0) {
            deviation = sentiment * MAX_UP.doubleValue() * confidence;
        } else {
            deviation = sentiment * MAX_DOWN.doubleValue() * confidence;
        }

        double result = BASE_TEMPERATURE.doubleValue() + deviation;
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
    }
}
