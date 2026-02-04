package com.mapleraid.party.application.port.in.input.query;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadAllAvailabilityInput {

    private final PartyRoomId partyRoomId;
    private final UserId requesterId;

    private ReadAllAvailabilityInput(PartyRoomId partyRoomId, UserId requesterId) {
        this.partyRoomId = partyRoomId;
        this.requesterId = requesterId;
    }

    public static ReadAllAvailabilityInput of(PartyRoomId partyRoomId, UserId requesterId) {
        return new ReadAllAvailabilityInput(partyRoomId, requesterId);
    }
}
