package com.mapleraid.party.application.port.in.input.query;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ReadPartyChatMessagesInput {

    private final PartyRoomId partyRoomId;
    private final UserId requesterId;
    private final int limit;
    private final Instant before; // nullable

    private ReadPartyChatMessagesInput(PartyRoomId partyRoomId, UserId requesterId, int limit, Instant before) {
        this.partyRoomId = partyRoomId;
        this.requesterId = requesterId;
        this.limit = limit;
        this.before = before;
    }

    public static ReadPartyChatMessagesInput of(PartyRoomId partyRoomId, UserId requesterId, int limit, Instant before) {
        return new ReadPartyChatMessagesInput(partyRoomId, requesterId, limit, before);
    }
}
