package com.mapleraid.party.application.port.in.usecase;

import com.mapleraid.core.annotation.bean.UseCase;
import com.mapleraid.party.application.port.in.input.query.ReadMyPartyRoomsInput;
import com.mapleraid.party.application.port.in.output.result.ReadMyPartyRoomsResult;

@UseCase
public interface ReadMyPartyRoomsUseCase {

    ReadMyPartyRoomsResult execute(ReadMyPartyRoomsInput input);
}
