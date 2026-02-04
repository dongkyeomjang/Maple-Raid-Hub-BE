package com.mapleraid.chat.domain;

import com.mapleraid.core.BaseId;

import java.util.UUID;

public class DirectMessageId extends BaseId<UUID> {

    private DirectMessageId(UUID value) {
        super(value);
    }

    public static DirectMessageId of(String value) {
        return new DirectMessageId(UUID.fromString(value));
    }

    public static DirectMessageId of(UUID value) {
        return new DirectMessageId(value);
    }

    public static DirectMessageId generate() {
        return new DirectMessageId(generateUUID());
    }
}
