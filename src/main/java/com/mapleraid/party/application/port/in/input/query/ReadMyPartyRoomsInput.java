package com.mapleraid.party.application.port.in.input.query;

import com.mapleraid.party.domain.PartyRoomStatus;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadMyPartyRoomsInput {

    private final UserId userId;
    private final PartyRoomStatus status; // nullable

    private ReadMyPartyRoomsInput(UserId userId, PartyRoomStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public static ReadMyPartyRoomsInput of(UserId userId, PartyRoomStatus status) {
        return new ReadMyPartyRoomsInput(userId, status);
    }
}
