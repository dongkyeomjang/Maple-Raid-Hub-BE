package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.SubmitReviewsInput;
import com.mapleraid.party.application.port.in.output.result.SubmitReviewsResult;
import com.mapleraid.party.application.port.in.usecase.SubmitReviewsUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.review.application.port.out.ReviewRepository;
import com.mapleraid.review.domain.Review;
import com.mapleraid.user.application.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmitReviewsService implements SubmitReviewsUseCase {
    private final PartyRoomRepository partyRoomRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubmitReviewsResult execute(SubmitReviewsInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));

        if (!partyRoom.isCompleted()) {
            throw new CommonException(ErrorCode.PARTY_NOT_COMPLETED);
        }
        if (!partyRoom.isMember(input.getReviewerId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }

        List<Review> savedReviews = input.getReviews().stream().map(reviewItem -> {
            if (reviewRepository.existsByPartyRoomIdAndReviewerIdAndRevieweeId(
                    input.getPartyRoomId(), input.getReviewerId(), reviewItem.targetUserId())) {
                throw new CommonException(ErrorCode.REVIEW_ALREADY_SUBMITTED);
            }
            if (!partyRoom.isMember(reviewItem.targetUserId())) {
                throw new CommonException(ErrorCode.REVIEW_INVALID_TARGET);
            }
            Review review = Review.create(
                    input.getPartyRoomId(),
                    input.getReviewerId(),
                    reviewItem.targetUserId(),
                    reviewItem.tags(),
                    reviewItem.comment(),
                    partyRoom.getCompletedAt()
            );
            Review savedReview = reviewRepository.save(review);
            userRepository.findById(reviewItem.targetUserId()).ifPresent(user -> {
                user.adjustTemperature(review.getTemperatureChange());
                userRepository.save(user);
            });
            return savedReview;
        }).toList();

        String reviewerNickname = userRepository.findById(input.getReviewerId())
                .map(user -> user.getNickname())
                .orElse("Unknown");

        return SubmitReviewsResult.from(savedReviews, reviewerNickname);
    }
}
