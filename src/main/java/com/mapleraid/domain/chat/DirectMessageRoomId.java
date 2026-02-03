package com.mapleraid.domain.chat;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class DirectMessageRoomId extends BaseId<UUID> {

    private DirectMessageRoomId(UUID value) {
        super(value);
    }

    public static DirectMessageRoomId of(String value) {
        return new DirectMessageRoomId(UUID.fromString(value));
    }

    public static DirectMessageRoomId of(UUID value) {
        return new DirectMessageRoomId(value);
    }

    public static DirectMessageRoomId generate() {
        return new DirectMessageRoomId(generateUUID());
    }
}
