package com.mapleraid.manner.adapter.in.web.query;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.manner.adapter.in.web.dto.EvaluationAvailabilityDto;
import com.mapleraid.manner.adapter.in.web.dto.MyEvaluationDetailDto;
import com.mapleraid.manner.application.port.in.input.CheckEvaluationAvailabilityInput;
import com.mapleraid.manner.adapter.in.web.dto.TagSummaryDto;
import com.mapleraid.manner.application.port.in.usecase.CheckEvaluationAvailabilityUseCase;
import com.mapleraid.manner.application.port.in.usecase.GetMyEvaluationsUseCase;
import com.mapleraid.manner.application.port.in.usecase.GetUserTagSummaryUseCase;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manner")
@RequiredArgsConstructor
public class MannerQueryController {

    private final GetMyEvaluationsUseCase getMyEvaluationsUseCase;
    private final CheckEvaluationAvailabilityUseCase checkEvaluationAvailabilityUseCase;
    private final GetUserTagSummaryUseCase getUserTagSummaryUseCase;

    /**
     * 내가 받은 매너 평가 조회
     */
    @GetMapping("/my-evaluations")
    public ResponseDto<List<MyEvaluationDetailDto>> getMyEvaluations(
            @CurrentUser UserId userId) {
        return ResponseDto.ok(
                getMyEvaluationsUseCase.execute(userId).getEvaluations().stream()
                        .map(MyEvaluationDetailDto::from)
                        .toList()
        );
    }

    /**
     * 평가 가능 여부 확인
     */
    @GetMapping("/check")
    public ResponseDto<EvaluationAvailabilityDto> checkAvailability(
            @CurrentUser UserId userId,
            @RequestParam String targetUserId) {
        return ResponseDto.ok(
                EvaluationAvailabilityDto.from(
                        checkEvaluationAvailabilityUseCase.execute(
                                CheckEvaluationAvailabilityInput.of(
                                        userId,
                                        UserId.of(targetUserId)
                                )
                        )
                )
        );
    }

    /**
     * 특정 사용자의 받은 태그 요약
     */
    @GetMapping("/user/{userId}/tag-summary")
    public ResponseDto<TagSummaryDto> getUserTagSummary(
            @PathVariable String userId) {
        return ResponseDto.ok(
                TagSummaryDto.from(
                        getUserTagSummaryUseCase.execute(UserId.of(userId))
                )
        );
    }
}
