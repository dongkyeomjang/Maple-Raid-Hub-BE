package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadMyApplicationsResult;

import java.time.Instant;
import java.util.List;

public record MyApplicationResponseDto(
        String id,
        String postId,
        String applicantId,
        String characterId,
        String message,
        String status,
        Instant appliedAt,
        Instant respondedAt,
        List<String> bossIds,
        String postStatus,
        int requiredMembers,
        int currentMembers,
        String authorCharacterName,
        String authorCharacterImageUrl,
        String authorWorldName
) {
    public static MyApplicationResponseDto from(ReadMyApplicationsResult.ApplicationSummary summary) {
        return new MyApplicationResponseDto(
                summary.getId(), summary.getPostId(), summary.getApplicantId(),
                summary.getCharacterId(), summary.getMessage(), summary.getStatus(),
                summary.getAppliedAt(), summary.getRespondedAt(),
                summary.getBossIds(), summary.getPostStatus(),
                summary.getRequiredMembers(), summary.getCurrentMembers(),
                summary.getAuthorCharacterName(), summary.getAuthorCharacterImageUrl(),
                summary.getAuthorWorldName()
        );
    }
}
