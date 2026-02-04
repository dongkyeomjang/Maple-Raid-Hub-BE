package com.mapleraid.review.application.port.out;

import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.review.domain.Review;
import com.mapleraid.review.domain.ReviewId;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(ReviewId id);

    List<Review> findByRevieweeId(UserId revieweeId);

    List<Review> findByPartyRoomId(PartyRoomId partyRoomId);

    boolean existsByPartyRoomIdAndReviewerIdAndRevieweeId(
            PartyRoomId partyRoomId, UserId reviewerId, UserId revieweeId);
}
