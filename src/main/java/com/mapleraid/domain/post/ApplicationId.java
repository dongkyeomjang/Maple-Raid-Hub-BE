package com.mapleraid.domain.post;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class ApplicationId extends BaseId<UUID> {

    private ApplicationId(UUID value) {
        super(value);
    }

    public static ApplicationId generate() {
        return new ApplicationId(generateUUID());
    }

    public static ApplicationId of(UUID value) {
        return new ApplicationId(value);
    }

    public static ApplicationId of(String value) {
        return new ApplicationId(UUID.fromString(value));
    }
}
