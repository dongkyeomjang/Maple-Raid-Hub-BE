package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.ReviewJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.ReviewJpaRepository;
import com.mapleraid.application.port.out.ReviewRepository;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.review.ReviewId;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;

    public ReviewRepositoryAdapter(ReviewJpaRepository jpaRepository) {
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
