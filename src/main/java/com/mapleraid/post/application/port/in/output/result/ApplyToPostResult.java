package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.Application;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ApplyToPostResult extends SelfValidating<ApplyToPostResult> {

    private final String id;
    private final String applicantId;
    private final String characterId;
    private final String message;
    private final String status;
    private final Instant appliedAt;

    public ApplyToPostResult(String id, String applicantId, String characterId,
                             String message, String status, Instant appliedAt) {
        this.id = id;
        this.applicantId = applicantId;
        this.characterId = characterId;
        this.message = message;
        this.status = status;
        this.appliedAt = appliedAt;
        this.validateSelf();
    }

    public static ApplyToPostResult from(Application app) {
        return new ApplyToPostResult(
                app.getId().getValue().toString(),
                app.getApplicantId().getValue().toString(),
                app.getCharacterId().getValue().toString(),
                app.getMessage(),
                app.getStatus().name(),
                app.getAppliedAt());
    }
}
