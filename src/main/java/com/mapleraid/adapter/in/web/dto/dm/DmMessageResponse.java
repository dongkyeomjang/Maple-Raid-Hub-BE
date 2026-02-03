package com.mapleraid.adapter.in.web.dto.dm;

import com.mapleraid.domain.chat.DirectMessage;

import java.time.Instant;

public record DmMessageResponse(
        String id,
        String roomId,
        String senderId,
        String senderNickname,
        String senderCharacterId,
        String senderCharacterName,
        String senderCharacterImageUrl,
        String content,
        String type,
        boolean isRead,
        Instant createdAt
) {
    public static DmMessageResponse from(DirectMessage message, String senderNickname) {
        return from(message, senderNickname, null, null);
    }

    public static DmMessageResponse from(DirectMessage message, String senderNickname,
                                         String characterName, String characterImageUrl) {
        return new DmMessageResponse(
                message.getId().getValue().toString(),
                message.getRoomId().getValue().toString(),
                message.getSenderId() != null ? message.getSenderId().getValue().toString() : null,
                senderNickname,
                message.getSenderCharacterId() != null ? message.getSenderCharacterId().getValue().toString() : null,
                characterName,
                characterImageUrl,
                message.getContent(),
                message.getType().name(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}
