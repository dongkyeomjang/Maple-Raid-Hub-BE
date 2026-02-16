package com.mapleraid.manner.domain;

import com.mapleraid.core.BaseId;

import java.util.UUID;

public class MannerEvaluationId extends BaseId<UUID> {

    private MannerEvaluationId(UUID value) {
        super(value);
    }

    public static MannerEvaluationId generate() {
        return new MannerEvaluationId(generateUUID());
    }

    public static MannerEvaluationId of(UUID value) {
        return new MannerEvaluationId(value);
    }

    public static MannerEvaluationId of(String value) {
        return new MannerEvaluationId(UUID.fromString(value));
    }
}
