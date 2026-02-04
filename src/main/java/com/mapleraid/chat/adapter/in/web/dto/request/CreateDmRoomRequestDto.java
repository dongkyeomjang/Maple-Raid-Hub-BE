package com.mapleraid.chat.adapter.in.web.dto.request;

public record CreateDmRoomRequestDto(
        String postId,
        String targetUserId,
        String senderCharacterId,
        String targetCharacterId
) {
}
