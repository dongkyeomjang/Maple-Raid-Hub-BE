package com.mapleraid.adapter.in.web.dto.party;

import com.mapleraid.domain.review.ReviewTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReviewRequest(
        @NotBlank(message = "대상 사용자 ID는 필수입니다.")
        String targetUserId,

        @NotNull(message = "리뷰 태그는 필수입니다.")
        List<ReviewTag> tags,

        String comment
) {
}
