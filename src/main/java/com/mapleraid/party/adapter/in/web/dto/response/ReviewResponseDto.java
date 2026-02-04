package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.SubmitReviewsResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReviewResponseDto(
        String id,
        String partyRoomId,
        String reviewerNickname,
        String revieweeId,
        List<String> tags,
        BigDecimal temperatureChange,
        Instant createdAt
) {
    public static ReviewResponseDto from(SubmitReviewsResult.ReviewSummary summary) {
        return new ReviewResponseDto(
                summary.getId(),
                summary.getPartyRoomId(),
                summary.getReviewerNickname(),
                summary.getRevieweeId(),
                summary.getTags(),
                summary.getTemperatureChange(),
                summary.getCreatedAt()
        );
    }
}
