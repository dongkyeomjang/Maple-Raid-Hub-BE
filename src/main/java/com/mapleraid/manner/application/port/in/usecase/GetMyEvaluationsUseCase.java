package com.mapleraid.manner.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.manner.application.port.in.output.GetMyEvaluationsResult;
import com.mapleraid.user.domain.UserId;

@UseCase
public interface GetMyEvaluationsUseCase {
    GetMyEvaluationsResult execute(UserId evaluateeId);
}
