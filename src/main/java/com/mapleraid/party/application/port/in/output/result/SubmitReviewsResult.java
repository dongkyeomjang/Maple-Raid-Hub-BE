package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.review.domain.Review;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class SubmitReviewsResult extends SelfValidating<SubmitReviewsResult> {

    private final List<ReviewSummary> reviews;

    public SubmitReviewsResult(List<ReviewSummary> reviews) {
        this.reviews = reviews;
        this.validateSelf();
    }

    public static SubmitReviewsResult from(List<Review> reviews, String reviewerNickname) {
        List<ReviewSummary> summaries = reviews.stream()
                .map(r -> new ReviewSummary(
                        r.getId().getValue().toString(),
                        r.getPartyRoomId().getValue().toString(),
                        reviewerNickname,
                        r.getRevieweeId().getValue().toString(),
                        r.getTags().stream().map(Enum::name).toList(),
                        r.getTemperatureChange(),
                        r.getCreatedAt()))
                .toList();
        return new SubmitReviewsResult(summaries);
    }

    @Getter
    public static class ReviewSummary {

        private final String id;
        private final String partyRoomId;
        private final String reviewerNickname;
        private final String revieweeId;
        private final List<String> tags;
        private final BigDecimal temperatureChange;
        private final Instant createdAt;

        public ReviewSummary(String id, String partyRoomId, String reviewerNickname,
                             String revieweeId, List<String> tags, BigDecimal temperatureChange,
                             Instant createdAt) {
            this.id = id;
            this.partyRoomId = partyRoomId;
            this.reviewerNickname = reviewerNickname;
            this.revieweeId = revieweeId;
            this.tags = tags;
            this.temperatureChange = temperatureChange;
            this.createdAt = createdAt;
        }
    }
}
