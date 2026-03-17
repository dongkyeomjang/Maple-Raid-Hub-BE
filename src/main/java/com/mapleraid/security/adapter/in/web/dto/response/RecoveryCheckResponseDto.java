package com.mapleraid.security.adapter.in.web.dto.response;

import com.mapleraid.security.application.port.in.output.result.CheckRecoveryChallengeResult;

public record RecoveryCheckResponseDto(
        String status,
        String message,
        Integer remainingChecks,
        Long remainingTimeSeconds,
        String username,
        String recoveryToken
) {
    public static RecoveryCheckResponseDto from(CheckRecoveryChallengeResult result) {
        return new RecoveryCheckResponseDto(
                result.getStatus().name(),
                result.getMessage(),
                result.getRemainingChecks(),
                result.getRemainingTimeSeconds(),
                result.getUsername(),
                result.getRecoveryToken()
        );
    }
}
