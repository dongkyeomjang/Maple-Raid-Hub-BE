package com.mapleraid.application.port.out;

import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.review.Review;
import com.mapleraid.domain.review.ReviewId;
import com.mapleraid.domain.user.UserId;

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
