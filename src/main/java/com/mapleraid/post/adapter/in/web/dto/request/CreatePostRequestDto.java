package com.mapleraid.post.adapter.in.web.dto.request;

import java.util.List;

public record CreatePostRequestDto(
        String characterId,
        List<String> bossIds,
        int requiredMembers,
        String preferredTime,
        String description
) {
}
