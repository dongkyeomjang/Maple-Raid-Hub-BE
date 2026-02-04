package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.Application;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RejectApplicationResult extends SelfValidating<RejectApplicationResult> {

    private final String id;
    private final String status;
    private final Instant respondedAt;

    public RejectApplicationResult(String id, String status, Instant respondedAt) {
        this.id = id;
        this.status = status;
        this.respondedAt = respondedAt;
        this.validateSelf();
    }

    public static RejectApplicationResult from(Application application) {
        return new RejectApplicationResult(
                application.getId().getValue().toString(),
                application.getStatus().name(),
                application.getRespondedAt());
    }
}
