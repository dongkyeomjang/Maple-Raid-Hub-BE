package com.mapleraid.manner.application.service;

import com.mapleraid.manner.application.port.in.output.GetMyEvaluationsResult;
import com.mapleraid.manner.application.port.in.usecase.GetMyEvaluationsUseCase;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetMyEvaluationsService implements GetMyEvaluationsUseCase {

    private final MannerEvaluationRepository mannerEvaluationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public GetMyEvaluationsResult execute(UserId evaluateeId) {
        List<MannerEvaluation> evaluations = mannerEvaluationRepository.findByEvaluateeId(evaluateeId);

        // evaluator ID들 수집하여 한번에 조회
        List<UserId> evaluatorIds = evaluations.stream()
                .map(MannerEvaluation::getEvaluatorId)
                .distinct()
                .toList();
        Map<UserId, User> evaluatorMap = userRepository.findAllByIds(evaluatorIds);

        List<GetMyEvaluationsResult.EvaluationDetail> details = evaluations.stream()
                .map(eval -> {
                    String nickname = evaluatorMap.containsKey(eval.getEvaluatorId())
                            ? evaluatorMap.get(eval.getEvaluatorId()).getNickname()
                            : "알 수 없음";
                    return new GetMyEvaluationsResult.EvaluationDetail(
                            eval.getId().getValue().toString(),
                            nickname,
                            eval.getContext().name(),
                            eval.getTags().stream().map(Enum::name).toList(),
                            eval.getTemperatureChange(),
                            eval.getCreatedAt()
                    );
                })
                .toList();

        return new GetMyEvaluationsResult(details);
    }
}
