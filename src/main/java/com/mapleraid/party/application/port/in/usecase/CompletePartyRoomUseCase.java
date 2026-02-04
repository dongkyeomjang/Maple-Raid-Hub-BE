package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.CompletePartyRoomInput;
import com.mapleraid.party.application.port.in.output.result.CompletePartyRoomResult;

@UseCase
public interface CompletePartyRoomUseCase {

    CompletePartyRoomResult execute(CompletePartyRoomInput input);
}
