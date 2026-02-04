package com.mapleraid.chat.adapter.in.web.dto.response;

import com.mapleraid.chat.application.port.in.output.result.CreateDmRoomResult;
import com.mapleraid.chat.application.port.in.output.result.ReadDmRoomResult;
import com.mapleraid.chat.application.port.in.output.result.ReadMyDmRoomsResult;

import java.time.Instant;

public record DmRoomResponseDto(
        String id,
        String postId,
        String otherUserId,
        String otherUserNickname,
        String otherCharacterId,
        String otherCharacterName,
        String otherCharacterImageUrl,
        String myCharacterId,
        String myCharacterName,
        String myCharacterImageUrl,
        int unreadCount,
        String lastMessageContent,
        Instant lastMessageAt,
        Instant createdAt
) {
    public static DmRoomResponseDto from(CreateDmRoomResult result) {
        return new DmRoomResponseDto(
                result.getId(),
                result.getPostId(),
                result.getUser2Id(),
                null,
                result.getUser2CharacterId(),
                null, null,
                null, null, null,
                result.getUnreadCountUser1(),
                null,
                result.getLastMessageAt(),
                result.getCreatedAt()
        );
    }

    public static DmRoomResponseDto from(ReadDmRoomResult result) {
        return new DmRoomResponseDto(
                result.getId(),
                result.getPostId(),
                result.getUser2Id(),
                null,
                result.getUser2CharacterId(),
                null, null,
                null, null, null,
                result.getUnreadCountUser1(),
                null,
                result.getLastMessageAt(),
                result.getCreatedAt()
        );
    }

    public static DmRoomResponseDto from(ReadMyDmRoomsResult.DmRoomSummary summary) {
        return new DmRoomResponseDto(
                summary.getId(),
                summary.getPostId(),
                summary.getOtherUserId(),
                summary.getOtherUserNickname(),
                summary.getOtherCharacterId(),
                summary.getOtherCharacterName(),
                summary.getOtherCharacterImageUrl(),
                summary.getMyCharacterId(),
                summary.getMyCharacterName(),
                summary.getMyCharacterImageUrl(),
                summary.getUnreadCount(),
                summary.getLastMessageContent(),
                summary.getLastMessageAt(),
                summary.getCreatedAt()
        );
    }
}
