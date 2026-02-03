package com.mapleraid.domain.character;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class CharacterId extends BaseId<UUID> {

    private CharacterId(UUID value) {
        super(value);
    }

    public static CharacterId generate() {
        return new CharacterId(generateUUID());
    }

    public static CharacterId of(UUID value) {
        return new CharacterId(value);
    }

    public static CharacterId of(String value) {
        return new CharacterId(UUID.fromString(value));
    }
}
