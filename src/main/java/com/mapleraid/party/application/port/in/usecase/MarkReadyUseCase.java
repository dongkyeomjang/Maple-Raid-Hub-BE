package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.MarkReadyInput;
import com.mapleraid.party.application.port.in.output.result.MarkReadyResult;

@UseCase
public interface MarkReadyUseCase {

    MarkReadyResult execute(MarkReadyInput input);
}
