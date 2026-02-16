package com.mapleraid.manner.application.port.in.input;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CheckEvaluationAvailabilityInput extends SelfValidating<CheckEvaluationAvailabilityInput> {

    @NotNull(message = "평가자 아이디는 필수입니다.")
    private final UserId evaluatorId;

    @NotNull(message = "평가 대상 아이디는 필수입니다.")
    private final UserId targetUserId;

    private CheckEvaluationAvailabilityInput(UserId evaluatorId, UserId targetUserId) {
        this.evaluatorId = evaluatorId;
        this.targetUserId = targetUserId;
        this.validateSelf();
    }

    public static CheckEvaluationAvailabilityInput of(UserId evaluatorId, UserId targetUserId) {
        return new CheckEvaluationAvailabilityInput(evaluatorId, targetUserId);
    }
}
