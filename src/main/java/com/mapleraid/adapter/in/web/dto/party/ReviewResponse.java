package com.mapleraid.adapter.in.web.dto.party;

import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.review.ReviewTag;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReviewResponse(
        String id,
        String partyRoomId,
        String reviewerId,
        String revieweeId,
        List<ReviewTag> tags,
        String comment,
        BigDecimal temperatureChange,
        Instant createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId().getValue().toString(),
                review.getPartyRoomId().getValue().toString(),
                review.getReviewerId().getValue().toString(),
                review.getRevieweeId().getValue().toString(),
                review.getTags(),
                review.getComment(),
                review.getTemperatureChange(),
                review.getCreatedAt()
        );
    }
}
