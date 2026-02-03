package com.mapleraid.adapter.in.web.dto.verification;

import com.mapleraid.domain.character.ChallengeStatus;
import com.mapleraid.domain.character.VerificationChallenge;

import java.time.Instant;

public record ChallengeResponse(
        String id,
        String characterId,
        String requiredSymbol1,
        String requiredSymbol2,
        ChallengeStatus status,
        int checkCount,
        int maxChecks,
        Instant createdAt,
        Instant expiresAt,
        Instant lastCheckedAt,
        long secondsUntilNextCheck
) {
    public static ChallengeResponse from(VerificationChallenge challenge) {
        return new ChallengeResponse(
                challenge.getId().getValue().toString(),
                challenge.getCharacterId().getValue().toString(),
                challenge.getRequiredSymbol1(),
                challenge.getRequiredSymbol2(),
                challenge.getStatus(),
                challenge.getCheckCount(),
                challenge.getMaxChecks(),
                challenge.getCreatedAt(),
                challenge.getExpiresAt(),
                challenge.getLastCheckedAt(),
                challenge.getSecondsUntilNextCheck()
        );
    }
}
