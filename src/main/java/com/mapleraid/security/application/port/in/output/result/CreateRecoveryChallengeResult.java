package com.mapleraid.security.application.port.in.output.result;

import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateRecoveryChallengeResult extends SelfValidating<CreateRecoveryChallengeResult> {

    private final String id;
    private final String characterId;
    private final String requiredSymbol1;
    private final String requiredSymbol2;
    private final EChallengeStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final int maxChecks;

    public CreateRecoveryChallengeResult(
            String id, String characterId,
            String requiredSymbol1, String requiredSymbol2,
            EChallengeStatus status,
            LocalDateTime createdAt, LocalDateTime expiresAt,
            int maxChecks
    ) {
        this.id = id;
        this.characterId = characterId;
        this.requiredSymbol1 = requiredSymbol1;
        this.requiredSymbol2 = requiredSymbol2;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxChecks = maxChecks;
        this.validateSelf();
    }

    public static CreateRecoveryChallengeResult from(VerificationChallenge challenge) {
        return new CreateRecoveryChallengeResult(
                challenge.getId().getValue().toString(),
                challenge.getCharacterId().getValue().toString(),
                challenge.getRequiredSymbol1(),
                challenge.getRequiredSymbol2(),
                challenge.getStatus(),
                challenge.getCreatedAt(),
                challenge.getExpiresAt(),
                challenge.getMaxChecks()
        );
    }
}
