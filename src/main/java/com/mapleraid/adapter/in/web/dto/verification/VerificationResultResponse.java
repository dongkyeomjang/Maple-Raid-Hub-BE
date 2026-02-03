package com.mapleraid.adapter.in.web.dto.verification;

import com.mapleraid.domain.character.VerificationChallenge.VerificationResult;

public record VerificationResultResponse(
        String status,
        String message,
        Integer remainingChecks,
        Long remainingSeconds
) {
    public static VerificationResultResponse from(VerificationResult result) {
        return new VerificationResultResponse(
                result.status().name(),
                result.message(),
                result.remainingChecks(),
                result.remainingTime() != null ? result.remainingTime().getSeconds() : null
        );
    }
}
