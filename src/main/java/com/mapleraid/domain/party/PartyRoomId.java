package com.mapleraid.domain.party;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class PartyRoomId extends BaseId<UUID> {

    private PartyRoomId(UUID value) {
        super(value);
    }

    public static PartyRoomId generate() {
        return new PartyRoomId(generateUUID());
    }

    public static PartyRoomId of(UUID value) {
        return new PartyRoomId(value);
    }

    public static PartyRoomId of(String value) {
        return new PartyRoomId(UUID.fromString(value));
    }
}
