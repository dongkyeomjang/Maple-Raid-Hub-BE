package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.ConfirmScheduleInput;
import com.mapleraid.party.application.port.in.output.result.ConfirmScheduleResult;

@UseCase
public interface ConfirmScheduleUseCase {

    ConfirmScheduleResult execute(ConfirmScheduleInput input);
}
