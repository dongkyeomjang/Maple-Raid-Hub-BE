package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.StartReadyCheckInput;
import com.mapleraid.party.application.port.in.output.result.StartReadyCheckResult;

@UseCase
public interface StartReadyCheckUseCase {

    StartReadyCheckResult execute(StartReadyCheckInput input);
}
