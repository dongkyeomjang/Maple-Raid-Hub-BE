package com.mapleraid.character.application.port.in.input.command;

import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class CheckVerificationInput {

    private final VerificationChallengeId challengeId;

    private final UserId requesterId;

    public CheckVerificationInput(
            VerificationChallengeId challengeId,
            UserId requesterId
    ) {
        this.challengeId = challengeId;
        this.requesterId = requesterId;
    }

    public static CheckVerificationInput of(
            VerificationChallengeId challengeId,
            UserId requesterId
    ) {
        return new CheckVerificationInput(
                challengeId,
                requesterId
        );
    }
}
