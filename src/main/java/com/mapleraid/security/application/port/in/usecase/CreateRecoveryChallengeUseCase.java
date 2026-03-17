package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.CreateRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.CreateRecoveryChallengeResult;

@UseCase
public interface CreateRecoveryChallengeUseCase {

    CreateRecoveryChallengeResult execute(CreateRecoveryChallengeInput input);
}
