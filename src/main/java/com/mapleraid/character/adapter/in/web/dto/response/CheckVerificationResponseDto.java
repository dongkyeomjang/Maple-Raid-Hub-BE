package com.mapleraid.character.adapter.in.web.dto.response;

import com.mapleraid.character.application.port.in.output.result.CheckVerificationResult;

public record CheckVerificationResponseDto(
        String status,
        String message,
        Integer remainingChecks,
        Long remainingTimeSeconds
) {
    public static CheckVerificationResponseDto from(CheckVerificationResult result) {
        return new CheckVerificationResponseDto(
                result.getStatus().name(),
                result.getMessage(),
                result.getRemainingChecks(),
                result.getRemainingTimeSeconds()
        );
    }
}
