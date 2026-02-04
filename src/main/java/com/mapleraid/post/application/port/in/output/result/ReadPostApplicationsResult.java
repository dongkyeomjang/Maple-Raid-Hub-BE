package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.Application;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadPostApplicationsResult extends SelfValidating<ReadPostApplicationsResult> {

    private final List<ApplicationSummary> applications;

    public ReadPostApplicationsResult(List<ApplicationSummary> applications) {
        this.applications = applications;
        this.validateSelf();
    }

    @Getter
    public static class ApplicationSummary {

        private final String id;
        private final String applicantId;
        private final String characterId;
        private final String message;
        private final String status;
        private final Instant appliedAt;
        private final Instant respondedAt;

        public ApplicationSummary(String id, String applicantId, String characterId,
                                  String message, String status,
                                  Instant appliedAt, Instant respondedAt) {
            this.id = id;
            this.applicantId = applicantId;
            this.characterId = characterId;
            this.message = message;
            this.status = status;
            this.appliedAt = appliedAt;
            this.respondedAt = respondedAt;
        }

        public static ApplicationSummary from(Application app) {
            return new ApplicationSummary(
                    app.getId().getValue().toString(),
                    app.getApplicantId().getValue().toString(),
                    app.getCharacterId().getValue().toString(),
                    app.getMessage(),
                    app.getStatus().name(),
                    app.getAppliedAt(),
                    app.getRespondedAt());
        }
    }
}
