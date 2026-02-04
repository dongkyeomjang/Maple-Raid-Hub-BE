package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.command.LeavePartyRoomInput;

@UseCase
public interface LeavePartyRoomUseCase {

    void execute(LeavePartyRoomInput input);
}
