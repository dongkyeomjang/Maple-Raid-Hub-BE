package com.mapleraid.review.adapter.out.persistence;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.review.adapter.out.persistence.jpa.ReviewJpaEntity;
import com.mapleraid.review.adapter.out.persistence.jpa.ReviewJpaRepository;
import com.mapleraid.review.application.port.out.ReviewRepository;
import com.mapleraid.review.domain.Review;
import com.mapleraid.review.domain.ReviewId;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewPersistenceAdapter implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;

    public ReviewPersistenceAdapter(ReviewJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = ReviewJpaEntity.fromDomain(review);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Review> findById(ReviewId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(ReviewJpaEntity::toDomain);
    }

    @Override
    public boolean existsByPartyRoomIdAndReviewerIdAndRevieweeId(
            PartyRoomId partyRoomId, UserId reviewerId, UserId revieweeId) {
        return jpaRepository.existsByPartyRoomIdAndReviewerIdAndRevieweeId(
                partyRoomId.getValue().toString(), reviewerId.getValue().toString(), revieweeId.getValue().toString());
    }

    @Override
    public List<Review> findByPartyRoomId(PartyRoomId partyRoomId) {
        return jpaRepository.findByPartyRoomId(partyRoomId.getValue().toString()).stream()
                .map(ReviewJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByRevieweeId(UserId revieweeId) {
        return jpaRepository.findByRevieweeId(revieweeId.getValue().toString()).stream()
                .map(ReviewJpaEntity::toDomain)
                .toList();
    }
}
