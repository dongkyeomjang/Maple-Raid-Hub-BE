package com.mapleraid.character.application.port.in.output.result;

import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReadPendingChallengeResult extends SelfValidating<ReadPendingChallengeResult> {

    private final boolean exists;

    private final String id;

    private final String characterId;

    private final String requiredSymbol1;

    private final String requiredSymbol2;

    private final EChallengeStatus status;

    private final LocalDateTime createdAt;

    private final LocalDateTime expiresAt;

    private final int checkCount;

    private final int maxChecks;

    private final int remainingChecks;

    private final long remainingTimeSeconds;

    private final boolean canCheck;

    private final long secondsUntilNextCheck;

    public ReadPendingChallengeResult(
            boolean exists,
            String id,
            String characterId,
            String requiredSymbol1,
            String requiredSymbol2,
            EChallengeStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            int checkCount,
            int maxChecks,
            int remainingChecks,
            long remainingTimeSeconds,
            boolean canCheck,
            long secondsUntilNextCheck
    ) {
        this.exists = exists;
        this.id = id;
        this.characterId = characterId;
        this.requiredSymbol1 = requiredSymbol1;
        this.requiredSymbol2 = requiredSymbol2;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.checkCount = checkCount;
        this.maxChecks = maxChecks;
        this.remainingChecks = remainingChecks;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.canCheck = canCheck;
        this.secondsUntilNextCheck = secondsUntilNextCheck;

        this.validateSelf();
    }

    public static ReadPendingChallengeResult from(VerificationChallenge challenge) {
        return new ReadPendingChallengeResult(
                true,
                challenge.getId().getValue().toString(),
                challenge.getCharacterId().getValue().toString(),
                challenge.getRequiredSymbol1(),
                challenge.getRequiredSymbol2(),
                challenge.getStatus(),
                challenge.getCreatedAt(),
                challenge.getExpiresAt(),
                challenge.getCheckCount(),
                challenge.getMaxChecks(),
                challenge.getRemainingChecks(),
                challenge.getRemainingTime().getSeconds(),
                challenge.canCheck(),
                challenge.getSecondsUntilNextCheck()
        );
    }

    public static ReadPendingChallengeResult empty() {
        return new ReadPendingChallengeResult(
                false, null, null, null, null,
                null, null, null, 0, 0, 0, 0, false, 0
        );
    }
}
