package com.mapleraid.post.adapter.in.web.dto.request;

import java.util.List;

public record CreatePostRequestDto(
        // 회원 작성 시 필수
        String characterId,
        // 비회원 작성 시 필수
        Boolean guest,
        String guestWorldGroup,
        String guestWorldName,
        String guestCharacterName,
        String contactLink,
        String guestPassword,
        // 공통
        List<String> bossIds,
        int requiredMembers,
        String preferredTime,
        String description
) {
    public boolean isGuest() {
        return Boolean.TRUE.equals(guest);
    }
}
