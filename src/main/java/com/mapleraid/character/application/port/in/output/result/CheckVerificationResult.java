package com.mapleraid.character.application.port.in.output.result;

import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

@Getter
public class CheckVerificationResult extends SelfValidating<CheckVerificationResult> {

    private final Status status;

    private final String message;

    private final Integer remainingChecks;

    private final Long remainingTimeSeconds;

    public CheckVerificationResult(
            Status status,
            String message,
            Integer remainingChecks,
            Long remainingTimeSeconds
    ) {
        this.status = status;
        this.message = message;
        this.remainingChecks = remainingChecks;
        this.remainingTimeSeconds = remainingTimeSeconds;

        this.validateSelf();
    }

    public static CheckVerificationResult from(VerificationChallenge.VerificationResult result) {
        return new CheckVerificationResult(
                Status.valueOf(result.status().name()),
                result.message(),
                result.remainingChecks(),
                result.remainingTime() != null ? result.remainingTime().getSeconds() : null
        );
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS, NOT_YET, TOO_MANY_REMOVED, EXPIRED, MAX_CHECKS_EXCEEDED
    }
}
