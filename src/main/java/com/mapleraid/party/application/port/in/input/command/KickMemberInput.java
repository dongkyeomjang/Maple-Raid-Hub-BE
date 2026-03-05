package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class KickMemberInput {

    private final PartyRoomId partyRoomId;
    private final UserId requesterId;
    private final UserId targetUserId;

    private KickMemberInput(PartyRoomId partyRoomId, UserId requesterId, UserId targetUserId) {
        this.partyRoomId = partyRoomId;
        this.requesterId = requesterId;
        this.targetUserId = targetUserId;
    }

    public static KickMemberInput of(PartyRoomId partyRoomId, UserId requesterId, UserId targetUserId) {
        return new KickMemberInput(partyRoomId, requesterId, targetUserId);
    }
}
