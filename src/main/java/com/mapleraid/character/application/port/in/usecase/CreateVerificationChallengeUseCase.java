package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.command.CreateVerificationChallengeInput;
import com.mapleraid.character.application.port.in.output.result.CreateVerificationChallengeResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface CreateVerificationChallengeUseCase {

    CreateVerificationChallengeResult execute(CreateVerificationChallengeInput input);
}
