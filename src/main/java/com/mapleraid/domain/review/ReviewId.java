package com.mapleraid.domain.review;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class ReviewId extends BaseId<UUID> {

    private ReviewId(UUID value) {
        super(value);
    }

    public static ReviewId generate() {
        return new ReviewId(generateUUID());
    }

    public static ReviewId of(UUID value) {
        return new ReviewId(value);
    }

    public static ReviewId of(String value) {
        return new ReviewId(UUID.fromString(value));
    }
}
