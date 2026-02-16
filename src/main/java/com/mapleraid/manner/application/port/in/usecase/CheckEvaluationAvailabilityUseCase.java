package com.mapleraid.manner.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.manner.application.port.in.input.CheckEvaluationAvailabilityInput;
import com.mapleraid.manner.application.port.in.output.CheckEvaluationAvailabilityResult;

@UseCase
public interface CheckEvaluationAvailabilityUseCase {
    CheckEvaluationAvailabilityResult execute(CheckEvaluationAvailabilityInput input);
}
