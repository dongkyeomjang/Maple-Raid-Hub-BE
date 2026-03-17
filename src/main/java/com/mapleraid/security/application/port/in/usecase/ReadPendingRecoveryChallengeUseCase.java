package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.query.ReadPendingRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.ReadPendingRecoveryChallengeResult;

@UseCase
public interface ReadPendingRecoveryChallengeUseCase {

    ReadPendingRecoveryChallengeResult execute(ReadPendingRecoveryChallengeInput input);
}
