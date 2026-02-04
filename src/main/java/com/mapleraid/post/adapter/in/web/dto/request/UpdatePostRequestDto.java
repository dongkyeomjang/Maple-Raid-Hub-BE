package com.mapleraid.post.adapter.in.web.dto.request;

import java.util.List;

public record UpdatePostRequestDto(
        List<String> bossIds,
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
