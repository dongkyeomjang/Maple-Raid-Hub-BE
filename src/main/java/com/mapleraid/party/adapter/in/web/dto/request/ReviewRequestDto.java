package com.mapleraid.party.adapter.in.web.dto.request;

import com.mapleraid.review.domain.ReviewTag;

import java.util.List;

public record ReviewRequestDto(
        String targetUserId,
        List<ReviewTag> tags,
        String comment
) {
}
