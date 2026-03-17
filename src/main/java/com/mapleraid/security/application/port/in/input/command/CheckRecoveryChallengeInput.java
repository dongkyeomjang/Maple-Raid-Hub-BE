package com.mapleraid.security.application.port.in.input.command;

import com.mapleraid.character.domain.VerificationChallengeId;
import lombok.Getter;

@Getter
public class CheckRecoveryChallengeInput {

    private final VerificationChallengeId challengeId;

    public CheckRecoveryChallengeInput(VerificationChallengeId challengeId) {
        this.challengeId = challengeId;
    }

    public static CheckRecoveryChallengeInput of(VerificationChallengeId challengeId) {
        return new CheckRecoveryChallengeInput(challengeId);
    }
}
