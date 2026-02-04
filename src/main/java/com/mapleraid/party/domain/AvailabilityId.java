package com.mapleraid.party.domain;

import com.mapleraid.core.BaseId;

import java.util.UUID;

public class AvailabilityId extends BaseId<UUID> {

    private AvailabilityId(UUID value) {
        super(value);
    }

    public static AvailabilityId of(String value) {
        return new AvailabilityId(UUID.fromString(value));
    }

    public static AvailabilityId of(UUID value) {
        return new AvailabilityId(value);
    }

    public static AvailabilityId generate() {
        return new AvailabilityId(generateUUID());
    }
}
