package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadMyApplicationsResult extends SelfValidating<ReadMyApplicationsResult> {

    private final List<ApplicationSummary> applications;

    public ReadMyApplicationsResult(List<ApplicationSummary> applications) {
        this.applications = applications;
        this.validateSelf();
    }

    @Getter
    public static class ApplicationSummary {

        private final String id;
        private final String postId;
        private final String applicantId;
        private final String characterId;
        private final String message;
        private final String status;
        private final Instant appliedAt;
        private final Instant respondedAt;

        // Post summary fields
        private final List<String> bossIds;
        private final String postStatus;
        private final int requiredMembers;
        private final int currentMembers;
        private final String authorCharacterName;
        private final String authorCharacterImageUrl;
        private final String authorWorldName;

        public ApplicationSummary(String id, String postId, String applicantId, String characterId,
                                  String message, String status,
                                  Instant appliedAt, Instant respondedAt,
                                  List<String> bossIds, String postStatus,
                                  int requiredMembers, int currentMembers,
                                  String authorCharacterName, String authorCharacterImageUrl,
                                  String authorWorldName) {
            this.id = id;
            this.postId = postId;
            this.applicantId = applicantId;
            this.characterId = characterId;
            this.message = message;
            this.status = status;
            this.appliedAt = appliedAt;
            this.respondedAt = respondedAt;
            this.bossIds = bossIds;
            this.postStatus = postStatus;
            this.requiredMembers = requiredMembers;
            this.currentMembers = currentMembers;
            this.authorCharacterName = authorCharacterName;
            this.authorCharacterImageUrl = authorCharacterImageUrl;
            this.authorWorldName = authorWorldName;
        }
    }
}
