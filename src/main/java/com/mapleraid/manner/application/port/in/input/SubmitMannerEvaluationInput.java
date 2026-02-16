package com.mapleraid.manner.application.port.in.input;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.manner.domain.EvaluationContext;
import com.mapleraid.manner.domain.MannerTag;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class SubmitMannerEvaluationInput extends SelfValidating<SubmitMannerEvaluationInput> {

    @NotNull(message = "평가자 아이디는 필수입니다.")
    private final UserId evaluatorId;

    @NotNull(message = "평가 대상 아이디는 필수입니다.")
    private final UserId evaluateeId;

    @NotNull(message = "평가 맥락은 필수입니다.")
    private final EvaluationContext context;

    @NotEmpty(message = "태그는 최소 1개 이상 선택해야 합니다.")
    private final List<MannerTag> tags;

    private SubmitMannerEvaluationInput(UserId evaluatorId, UserId evaluateeId,
                                         EvaluationContext context, List<MannerTag> tags) {
        this.evaluatorId = evaluatorId;
        this.evaluateeId = evaluateeId;
        this.context = context;
        this.tags = tags;
        this.validateSelf();
    }

    public static SubmitMannerEvaluationInput of(UserId evaluatorId, UserId evaluateeId,
                                                   EvaluationContext context, List<MannerTag> tags) {
        return new SubmitMannerEvaluationInput(evaluatorId, evaluateeId, context, tags);
    }
}
