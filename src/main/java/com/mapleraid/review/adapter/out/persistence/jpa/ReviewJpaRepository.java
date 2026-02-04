package com.mapleraid.review.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, String> {

    boolean existsByPartyRoomIdAndReviewerIdAndRevieweeId(
            String partyRoomId, String reviewerId, String revieweeId);

    List<ReviewJpaEntity> findByPartyRoomId(String partyRoomId);

    List<ReviewJpaEntity> findByRevieweeId(String revieweeId);
}
