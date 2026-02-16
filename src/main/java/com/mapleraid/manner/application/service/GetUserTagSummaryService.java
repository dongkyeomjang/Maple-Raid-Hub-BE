package com.mapleraid.manner.application.service;

import com.mapleraid.manner.application.port.in.output.GetUserTagSummaryResult;
import com.mapleraid.manner.application.port.in.usecase.GetUserTagSummaryUseCase;
import com.mapleraid.manner.application.port.out.MannerEvaluationRepository;
import com.mapleraid.manner.domain.MannerEvaluation;
import com.mapleraid.manner.domain.MannerTag;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetUserTagSummaryService implements GetUserTagSummaryUseCase {

    private final MannerEvaluationRepository mannerEvaluationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public GetUserTagSummaryResult execute(UserId targetUserId) {
        List<MannerEvaluation> evaluations = mannerEvaluationRepository.findByEvaluateeId(targetUserId);

        Map<String, Integer> tagCountMap = new LinkedHashMap<>();
        for (MannerEvaluation evaluation : evaluations) {
            for (MannerTag tag : evaluation.getTags()) {
                tagCountMap.merge(tag.name(), 1, Integer::sum);
            }
        }

        List<GetUserTagSummaryResult.TagCount> tagCounts = tagCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> new GetUserTagSummaryResult.TagCount(e.getKey(), e.getValue()))
                .toList();

        double temperature = userRepository.findById(targetUserId)
                .map(User::getTemperature)
                .orElse(36.5);

        return new GetUserTagSummaryResult(tagCounts, evaluations.size(), temperature);
    }
}
