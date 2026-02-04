package com.mapleraid.post.domain;

import com.mapleraid.core.BaseId;

import java.util.UUID;

public class PostId extends BaseId<UUID> {

    private PostId(UUID value) {
        super(value);
    }

    public static PostId generate() {
        return new PostId(generateUUID());
    }

    public static PostId of(UUID value) {
        return new PostId(value);
    }

    public static PostId of(String value) {
        return new PostId(UUID.fromString(value));
    }
}
