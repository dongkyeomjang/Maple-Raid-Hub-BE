package com.mapleraid.adapter.out.persistence.entity;

import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.review.ReviewId;
import com.mapleraid.domain.review.ReviewTag;
import com.mapleraid.domain.user.UserId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "party_room_id", nullable = false, length = 36)
    private String partyRoomId;

    @Column(name = "reviewer_id", nullable = false, length = 36)
    private String reviewerId;

    @Column(name = "reviewee_id", nullable = false, length = 36)
    private String revieweeId;

    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    @Enumerated(EnumType.STRING)
    private List<ReviewTag> tags = new ArrayList<>();

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "temperature_change", precision = 4, scale = 2)
    private BigDecimal temperatureChange;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReviewJpaEntity() {
    }

    public static ReviewJpaEntity fromDomain(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity();
        entity.id = review.getId().getValue().toString();
        entity.partyRoomId = review.getPartyRoomId().getValue().toString();
        entity.reviewerId = review.getReviewerId().getValue().toString();
        entity.revieweeId = review.getRevieweeId().getValue().toString();
        entity.tags = new ArrayList<>(review.getTags());
        entity.comment = review.getComment();
        entity.temperatureChange = review.getTemperatureChange();
        entity.createdAt = review.getCreatedAt();
        return entity;
    }

    public Review toDomain() {
        return Review.reconstitute(
                ReviewId.of(id),
                PartyRoomId.of(partyRoomId),
                UserId.of(reviewerId),
                UserId.of(revieweeId),
                new ArrayList<>(tags),
                comment,
                temperatureChange,
                createdAt
        );
    }

    public String getId() {
        return id;
    }

    public String getPartyRoomId() {
        return partyRoomId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public String getRevieweeId() {
        return revieweeId;
    }
}
