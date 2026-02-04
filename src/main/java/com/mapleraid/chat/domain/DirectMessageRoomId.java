package com.mapleraid.chat.domain;

import com.mapleraid.core.BaseId;

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
