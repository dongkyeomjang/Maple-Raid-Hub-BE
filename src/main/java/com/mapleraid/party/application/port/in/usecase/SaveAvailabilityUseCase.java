package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.SaveAvailabilityInput;
import com.mapleraid.party.application.port.in.output.result.SaveAvailabilityResult;

@UseCase
public interface SaveAvailabilityUseCase {

    SaveAvailabilityResult execute(SaveAvailabilityInput input);
}
