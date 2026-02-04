package com.mapleraid.review.domain;

import com.mapleraid.core.BaseId;

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
