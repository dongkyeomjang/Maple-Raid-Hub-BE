package com.mapleraid.domain.user;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class UserId extends BaseId<UUID> {

    private UserId(UUID value) {
        super(value);
    }

    public static UserId generate() {
        return new UserId(generateUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }
}
