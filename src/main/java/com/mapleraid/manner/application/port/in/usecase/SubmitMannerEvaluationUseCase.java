package com.mapleraid.manner.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.manner.application.port.in.input.SubmitMannerEvaluationInput;
import com.mapleraid.manner.application.port.in.output.SubmitMannerEvaluationResult;

@UseCase
public interface SubmitMannerEvaluationUseCase {
    SubmitMannerEvaluationResult execute(SubmitMannerEvaluationInput input);
}
