package com.mapleraid.adapter.in.web.dto.post;

import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationStatus;

import java.time.Instant;

public record ApplicationResponse(
        String id,
        String applicantId,
        String characterId,
        String message,
        ApplicationStatus status,
        Instant appliedAt,
        Instant respondedAt
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId().getValue().toString(),
                application.getApplicantId().getValue().toString(),
                application.getCharacterId().getValue().toString(),
                application.getMessage(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getRespondedAt()
        );
    }
}
