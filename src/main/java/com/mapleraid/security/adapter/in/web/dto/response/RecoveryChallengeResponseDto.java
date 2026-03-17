package com.mapleraid.security.adapter.in.web.dto.response;

import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.security.application.port.in.output.result.CreateRecoveryChallengeResult;
import com.mapleraid.security.application.port.in.output.result.ReadPendingRecoveryChallengeResult;

import java.time.LocalDateTime;

public record RecoveryChallengeResponseDto(
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
        Integer remainingChecks,
        Long remainingTimeSeconds,
        Boolean canCheck,
        Long secondsUntilNextCheck
) {
    public static RecoveryChallengeResponseDto from(CreateRecoveryChallengeResult result) {
        return new RecoveryChallengeResponseDto(
                true,
                result.getId(),
                result.getCharacterId(),
                result.getRequiredSymbol1(),
                result.getRequiredSymbol2(),
                result.getStatus(),
                result.getCreatedAt(),
                result.getExpiresAt(),
                0,
                result.getMaxChecks(),
                result.getMaxChecks(),
                null,
                true,
                0L
        );
    }

    public static RecoveryChallengeResponseDto from(ReadPendingRecoveryChallengeResult result) {
        if (!result.isExists()) {
            return new RecoveryChallengeResponseDto(
                    false, null, null, null, null, null,
                    null, null, 0, 0, null, null, null, null
            );
        }
        return new RecoveryChallengeResponseDto(
                true,
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
