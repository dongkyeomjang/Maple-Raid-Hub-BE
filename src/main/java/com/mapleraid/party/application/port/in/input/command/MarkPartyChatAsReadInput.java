package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class MarkPartyChatAsReadInput {

    private final PartyRoomId partyRoomId;
    private final UserId userId;

    private MarkPartyChatAsReadInput(PartyRoomId partyRoomId, UserId userId) {
        this.partyRoomId = partyRoomId;
        this.userId = userId;
    }

    public static MarkPartyChatAsReadInput of(PartyRoomId partyRoomId, UserId userId) {
        return new MarkPartyChatAsReadInput(partyRoomId, userId);
    }
}
