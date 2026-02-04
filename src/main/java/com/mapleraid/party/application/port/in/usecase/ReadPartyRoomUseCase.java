package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.query.ReadPartyRoomInput;
import com.mapleraid.party.application.port.in.output.result.ReadPartyRoomResult;

@UseCase
public interface ReadPartyRoomUseCase {

    ReadPartyRoomResult execute(ReadPartyRoomInput input);
}
