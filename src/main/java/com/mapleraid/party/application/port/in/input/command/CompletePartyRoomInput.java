package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class CompletePartyRoomInput {

    private final PartyRoomId partyRoomId;
    private final UserId requesterId;

    private CompletePartyRoomInput(PartyRoomId partyRoomId, UserId requesterId) {
        this.partyRoomId = partyRoomId;
        this.requesterId = requesterId;
    }

    public static CompletePartyRoomInput of(PartyRoomId partyRoomId, UserId requesterId) {
        return new CompletePartyRoomInput(partyRoomId, requesterId);
    }
}
