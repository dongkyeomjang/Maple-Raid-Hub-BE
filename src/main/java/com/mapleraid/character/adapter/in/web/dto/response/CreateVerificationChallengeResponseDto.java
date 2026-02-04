package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.CreateVerificationChallengeResult;
import com.mapleraid.character.domain.type.EChallengeStatus;

import java.time.LocalDateTime;

public record CreateVerificationChallengeResponseDto(
        String id,
        String characterId,
        String requiredSymbol1,
        String requiredSymbol2,
        EChallengeStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        int maxChecks
) {
    public static CreateVerificationChallengeResponseDto from(CreateVerificationChallengeResult result) {
        return new CreateVerificationChallengeResponseDto(
                result.getId(),
                result.getCharacterId(),
                result.getRequiredSymbol1(),
                result.getRequiredSymbol2(),
                result.getStatus(),
                result.getCreatedAt(),
                result.getExpiresAt(),
                result.getMaxChecks()
        );
    }
}
