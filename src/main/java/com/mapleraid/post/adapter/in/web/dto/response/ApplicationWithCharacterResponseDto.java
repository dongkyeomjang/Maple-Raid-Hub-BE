package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadPostApplicationsResult;

import java.time.Instant;

public record ApplicationWithCharacterResponseDto(
        String id,
        String applicantId,
        String characterId,
        String message,
        String status,
        Instant appliedAt,
        Instant respondedAt,
        String characterName,
        String characterImageUrl
) {
    public static ApplicationWithCharacterResponseDto from(
            ReadPostApplicationsResult.ApplicationSummary summary,
            String characterName, String characterImageUrl) {
        return new ApplicationWithCharacterResponseDto(
                summary.getId(), summary.getApplicantId(), summary.getCharacterId(),
                summary.getMessage(), summary.getStatus(), summary.getAppliedAt(),
                summary.getRespondedAt(), characterName, characterImageUrl);
    }
}
