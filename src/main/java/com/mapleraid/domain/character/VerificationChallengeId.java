package com.mapleraid.domain.character;

import com.mapleraid.domain.common.BaseId;

import java.util.UUID;

public class VerificationChallengeId extends BaseId<UUID> {

    private VerificationChallengeId(UUID value) {
        super(value);
    }

    public static VerificationChallengeId generate() {
        return new VerificationChallengeId(generateUUID());
    }

    public static VerificationChallengeId of(UUID value) {
        return new VerificationChallengeId(value);
    }

    public static VerificationChallengeId of(String value) {
        return new VerificationChallengeId(UUID.fromString(value));
    }
}
