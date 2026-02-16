package com.mapleraid.manner.application.service;

import com.mapleraid.manner.application.port.in.output.GetMyEvaluationsResult;
import com.mapleraid.manner.application.port.in.usecase.GetMyEvaluationsUseCase;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyEvaluationsService implements GetMyEvaluationsUseCase {

    private final MannerEvaluationRepository mannerEvaluationRepository;

    @Override
    @Transactional(readOnly = true)
    public GetMyEvaluationsResult execute(UserId evaluateeId) {
        List<MannerEvaluation> evaluations = mannerEvaluationRepository.findByEvaluateeId(evaluateeId);

        List<GetMyEvaluationsResult.EvaluationDetail> details = evaluations.stream()
                .map(eval -> new GetMyEvaluationsResult.EvaluationDetail(
                        eval.getId().getValue().toString(),
                        eval.getContext().name(),
                        eval.getTags().stream().map(Enum::name).toList(),
                        eval.getTemperatureChange(),
                        eval.getCreatedAt()
                ))
                .toList();

        return new GetMyEvaluationsResult(details);
    }
}
