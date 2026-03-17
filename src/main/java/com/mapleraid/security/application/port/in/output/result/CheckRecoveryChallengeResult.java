package com.mapleraid.security.application.port.in.output.result;

import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

@Getter
public class CheckRecoveryChallengeResult extends SelfValidating<CheckRecoveryChallengeResult> {

    private final Status status;
    private final String message;
    private final Integer remainingChecks;
    private final Long remainingTimeSeconds;

    // 인증 성공 시에만 포함
    private final String username;
    private final String recoveryToken;

    public CheckRecoveryChallengeResult(
            Status status, String message,
            Integer remainingChecks, Long remainingTimeSeconds,
            String username, String recoveryToken
    ) {
        this.status = status;
        this.message = message;
        this.remainingChecks = remainingChecks;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.username = username;
        this.recoveryToken = recoveryToken;
        this.validateSelf();
    }

    public static CheckRecoveryChallengeResult success(String username, String recoveryToken) {
        return new CheckRecoveryChallengeResult(
                Status.SUCCESS,
                "인증이 완료되었습니다!",
                null, null,
                username, recoveryToken
        );
    }

    public static CheckRecoveryChallengeResult from(VerificationChallenge.VerificationResult result) {
        return new CheckRecoveryChallengeResult(
                Status.valueOf(result.status().name()),
                result.message(),
                result.remainingChecks(),
                result.remainingTime() != null ? result.remainingTime().getSeconds() : null,
                null, null
        );
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS, NOT_YET, TOO_MANY_REMOVED, EXPIRED, MAX_CHECKS_EXCEEDED
    }
}
