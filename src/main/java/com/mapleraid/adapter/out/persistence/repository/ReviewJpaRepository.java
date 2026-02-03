package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, String> {

    boolean existsByPartyRoomIdAndReviewerIdAndRevieweeId(
            String partyRoomId, String reviewerId, String revieweeId);

    List<ReviewJpaEntity> findByPartyRoomId(String partyRoomId);

    List<ReviewJpaEntity> findByRevieweeId(String revieweeId);
}
