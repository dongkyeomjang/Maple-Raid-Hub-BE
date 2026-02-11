package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ApplyToPostResult;
import com.mapleraid.post.application.port.in.output.result.ReadPostApplicationsResult;
import com.mapleraid.post.application.port.in.output.result.RejectApplicationResult;
import com.mapleraid.post.application.port.in.output.result.WithdrawApplicationResult;

import java.time.Instant;

public record ApplicationResponseDto(
        String id,
        String applicantId,
        String characterId,
        String message,
        String status,
        Instant appliedAt,
        Instant respondedAt
) {
    public static ApplicationResponseDto from(ApplyToPostResult result) {
        return new ApplicationResponseDto(result.getId(), result.getApplicantId(),
                result.getCharacterId(), result.getMessage(), result.getStatus(),
                result.getAppliedAt(), null);
    }

    public static ApplicationResponseDto from(RejectApplicationResult result) {
        return new ApplicationResponseDto(result.getId(), null, null, null,
                result.getStatus(), null, result.getRespondedAt());
    }

    public static ApplicationResponseDto from(WithdrawApplicationResult result) {
        return new ApplicationResponseDto(result.getId(), null, null, null,
                result.getStatus(), null, result.getRespondedAt());
    }

    public static ApplicationResponseDto from(ReadPostApplicationsResult.ApplicationSummary summary) {
        return new ApplicationResponseDto(summary.getId(), summary.getApplicantId(),
                summary.getCharacterId(), summary.getMessage(), summary.getStatus(),
                summary.getAppliedAt(), summary.getRespondedAt());
    }
}
