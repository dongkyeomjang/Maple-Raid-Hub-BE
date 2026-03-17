package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.security.application.port.in.input.command.CheckRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.CheckRecoveryChallengeResult;

@UseCase
public interface CheckRecoveryChallengeUseCase {

    CheckRecoveryChallengeResult execute(CheckRecoveryChallengeInput input);
}
