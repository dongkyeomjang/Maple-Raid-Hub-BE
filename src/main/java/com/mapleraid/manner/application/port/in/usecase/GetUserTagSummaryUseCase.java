package com.mapleraid.manner.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.manner.application.port.in.output.GetUserTagSummaryResult;
import com.mapleraid.user.domain.UserId;

@UseCase
public interface GetUserTagSummaryUseCase {
    GetUserTagSummaryResult execute(UserId targetUserId);
}
