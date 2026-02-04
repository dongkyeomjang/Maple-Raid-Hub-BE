package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.review.domain.ReviewTag;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class SubmitReviewsInput extends SelfValidating<SubmitReviewsInput> {

    @NotNull(message = "파티룸 아이디는 필수입니다.")
    private final PartyRoomId partyRoomId;

    @NotNull(message = "리뷰어 아이디는 필수입니다.")
    private final UserId reviewerId;

    @NotEmpty(message = "리뷰 목록은 비어있을 수 없습니다.")
    private final List<ReviewItem> reviews;

    private SubmitReviewsInput(PartyRoomId partyRoomId, UserId reviewerId, List<ReviewItem> reviews) {
        this.partyRoomId = partyRoomId;
        this.reviewerId = reviewerId;
        this.reviews = reviews;
        this.validateSelf();
    }

    public static SubmitReviewsInput of(PartyRoomId partyRoomId, UserId reviewerId, List<ReviewItem> reviews) {
        return new SubmitReviewsInput(partyRoomId, reviewerId, reviews);
    }

    public record ReviewItem(UserId targetUserId, List<ReviewTag> tags, String comment) {
    }
}
