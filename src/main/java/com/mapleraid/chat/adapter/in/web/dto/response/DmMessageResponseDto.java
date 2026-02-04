package com.mapleraid.chat.adapter.in.web.dto.response;

import com.mapleraid.chat.application.port.in.output.result.ReadDmMessagesResult;
import com.mapleraid.chat.application.port.in.output.result.SendDmMessageResult;

import java.time.Instant;

public record DmMessageResponseDto(
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
    public static DmMessageResponseDto from(SendDmMessageResult result) {
        return new DmMessageResponseDto(
                result.getId(),
                result.getRoomId(),
                result.getSenderId(),
                result.getSenderNickname(),
                result.getSenderCharacterId(),
                result.getSenderCharacterName(),
                result.getSenderCharacterImageUrl(),
                result.getContent(),
                result.getType(),
                result.isRead(),
                result.getCreatedAt()
        );
    }

    public static DmMessageResponseDto from(ReadDmMessagesResult.DmMessageItem item) {
        return new DmMessageResponseDto(
                item.getId(),
                item.getRoomId(),
                item.getSenderId(),
                item.getSenderNickname(),
                item.getSenderCharacterId(),
                item.getSenderCharacterName(),
                item.getSenderCharacterImageUrl(),
                item.getContent(),
                item.getType(),
                item.isRead(),
                item.getCreatedAt()
        );
    }
}
