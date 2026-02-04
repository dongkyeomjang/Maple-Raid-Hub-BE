package com.mapleraid.character.application.port.in.usecase;

import com.mapleraid.character.application.port.in.input.command.CheckVerificationInput;
import com.mapleraid.character.application.port.in.output.result.CheckVerificationResult;
import com.mapleraid.core.annotation.bean.UseCase;

@UseCase
public interface CheckVerificationUseCase {

    CheckVerificationResult execute(CheckVerificationInput input);
}
