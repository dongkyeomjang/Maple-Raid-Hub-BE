package com.mapleraid.review.domain;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 리뷰 엔티티
 */
public class Review {

    private static final Duration REVIEW_WINDOW = Duration.ofDays(7);
    private static final int MAX_TAGS = 3;
    private static final BigDecimal MAX_TEMPERATURE_CHANGE = new BigDecimal("1.0");

    private final ReviewId id;
    private final PartyRoomId partyRoomId;
    private final UserId reviewerId;
    private final UserId revieweeId;
    private final List<ReviewTag> tags;
    private final String comment;
    private final BigDecimal temperatureChange;
    private final Instant createdAt;

    private Review(ReviewId id, PartyRoomId partyRoomId, UserId reviewerId,
                   UserId revieweeId, List<ReviewTag> tags, String comment,
                   BigDecimal temperatureChange, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.partyRoomId = Objects.requireNonNull(partyRoomId);
        this.reviewerId = Objects.requireNonNull(reviewerId);
        this.revieweeId = Objects.requireNonNull(revieweeId);
        this.tags = List.copyOf(tags);
        this.comment = comment;
        this.temperatureChange = temperatureChange != null ? temperatureChange : calculateTemperatureChange(tags);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Review create(PartyRoomId partyRoomId, UserId reviewerId,
                                UserId revieweeId, List<ReviewTag> tags, String comment,
                                Instant partyCompletedAt) {
        // 본인 리뷰 불가
        if (reviewerId.equals(revieweeId)) {
            throw new CommonException(ErrorCode.REVIEW_SELF_REVIEW);
        }

        // 태그 개수 검증
        if (tags == null || tags.isEmpty()) {
            throw new CommonException(ErrorCode.REVIEW_NO_TAGS);
        }
        if (tags.size() > MAX_TAGS) {
            throw new CommonException(ErrorCode.REVIEW_TOO_MANY_TAGS);
        }

        // 리뷰 기간 검증
        if (partyCompletedAt != null) {
            Instant deadline = partyCompletedAt.plus(REVIEW_WINDOW);
            if (Instant.now().isAfter(deadline)) {
                throw new CommonException(ErrorCode.REVIEW_WINDOW_EXPIRED);
            }
        }

        return new Review(ReviewId.generate(), partyRoomId, reviewerId, revieweeId, tags, comment, null, null);
    }

    public static Review reconstitute(
            ReviewId id, PartyRoomId partyRoomId, UserId reviewerId, UserId revieweeId,
            List<ReviewTag> tags, String comment, BigDecimal temperatureChange, Instant createdAt) {
        return new Review(id, partyRoomId, reviewerId, revieweeId, tags, comment, temperatureChange, createdAt);
    }

    private static BigDecimal calculateTemperatureChange(List<ReviewTag> tags) {
        BigDecimal total = tags.stream()
                .map(ReviewTag::getTemperatureEffect)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 단일 리뷰로 최대 ±1°C 제한
        if (total.compareTo(MAX_TEMPERATURE_CHANGE) > 0) {
            return MAX_TEMPERATURE_CHANGE;
        }
        if (total.compareTo(MAX_TEMPERATURE_CHANGE.negate()) < 0) {
            return MAX_TEMPERATURE_CHANGE.negate();
        }
        return total;
    }

    // Getters
    public ReviewId getId() {
        return id;
    }

    public PartyRoomId getPartyRoomId() {
        return partyRoomId;
    }

    public UserId getReviewerId() {
        return reviewerId;
    }

    public UserId getRevieweeId() {
        return revieweeId;
    }

    public List<ReviewTag> getTags() {
        return tags;
    }

    public String getComment() {
        return comment;
    }

    public BigDecimal getTemperatureChange() {
        return temperatureChange;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
