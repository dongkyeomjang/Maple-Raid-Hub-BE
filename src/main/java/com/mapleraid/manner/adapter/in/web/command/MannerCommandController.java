package com.mapleraid.manner.adapter.in.web.command;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.manner.adapter.in.web.dto.MannerEvaluationRequestDto;
import com.mapleraid.manner.adapter.in.web.dto.MannerEvaluationResponseDto;
import com.mapleraid.manner.application.port.in.input.SubmitMannerEvaluationInput;
import com.mapleraid.manner.application.port.in.usecase.SubmitMannerEvaluationUseCase;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manner")
@RequiredArgsConstructor
public class MannerCommandController {

    private final SubmitMannerEvaluationUseCase submitMannerEvaluationUseCase;

    /**
     * 매너 평가 제출
     */
    @PostMapping("/evaluate")
    public ResponseDto<MannerEvaluationResponseDto> evaluate(
            @CurrentUser UserId userId,
            @RequestBody MannerEvaluationRequestDto request) {
        return ResponseDto.ok(
                MannerEvaluationResponseDto.from(
                        submitMannerEvaluationUseCase.execute(
                                SubmitMannerEvaluationInput.of(
                                        userId,
                                        UserId.of(request.targetUserId()),
                                        request.context(),
                                        request.tags()
                                )
                        )
                )
        );
    }
}
