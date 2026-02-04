package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.ReadPendingChallengeResult;
import com.mapleraid.character.domain.type.EChallengeStatus;

import java.time.LocalDateTime;

public record ReadPendingChallengeResponseDto(
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
    public static ReadPendingChallengeResponseDto from(ReadPendingChallengeResult result) {
        return new ReadPendingChallengeResponseDto(
                result.isExists(),
                result.getId(),
                result.getCharacterId(),
                result.getRequiredSymbol1(),
                result.getRequiredSymbol2(),
                result.getStatus(),
                result.getCreatedAt(),
                result.getExpiresAt(),
                result.getCheckCount(),
                result.getMaxChecks(),
                result.getRemainingChecks(),
                result.getRemainingTimeSeconds(),
                result.isCanCheck(),
                result.getSecondsUntilNextCheck()
        );
    }
}
