package com.mapleraid.adapter.in.web.dto.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record UpdatePostRequest(
        List<String> bossIds,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        @Max(value = 6, message = "최대 6명까지 가능합니다.")
        Integer requiredMembers,

        String preferredTime,

        Boolean clearPreferredTime,

        String description,

        Boolean clearDescription
) {
    public boolean shouldClearPreferredTime() {
        return Boolean.TRUE.equals(clearPreferredTime);
    }

    public boolean shouldClearDescription() {
        return Boolean.TRUE.equals(clearDescription);
    }
}
