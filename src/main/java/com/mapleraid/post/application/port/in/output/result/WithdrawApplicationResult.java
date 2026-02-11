package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.Application;
import lombok.Getter;

import java.time.Instant;

@Getter
public class WithdrawApplicationResult extends SelfValidating<WithdrawApplicationResult> {

    private final String id;
    private final String status;
    private final Instant respondedAt;

    public WithdrawApplicationResult(String id, String status, Instant respondedAt) {
        this.id = id;
        this.status = status;
        this.respondedAt = respondedAt;
        this.validateSelf();
    }

    public static WithdrawApplicationResult from(Application application) {
        return new WithdrawApplicationResult(
                application.getId().getValue().toString(),
                application.getStatus().name(),
                application.getRespondedAt());
    }
}
