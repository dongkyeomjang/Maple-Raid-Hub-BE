package com.mapleraid.adapter.in.web.dto.dm;

import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.user.UserId;

import java.time.Instant;

public record DmRoomResponse(
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
    public static DmRoomResponse from(DirectMessageRoom room, UserId currentUserId,
                                      String otherUserNickname, String lastMessageContent,
                                      String otherCharacterName, String otherCharacterImageUrl,
                                      String myCharacterName, String myCharacterImageUrl) {
        var otherCharId = room.getOtherUserCharacterId(currentUserId);
        var myCharId = room.getMyCharacterId(currentUserId);
        return new DmRoomResponse(
                room.getId().getValue().toString(),
                room.getPostId() != null ? room.getPostId().getValue().toString() : null,
                room.getOtherUser(currentUserId).getValue().toString(),
                otherUserNickname,
                otherCharId != null ? otherCharId.getValue().toString() : null,
                otherCharacterName,
                otherCharacterImageUrl,
                myCharId != null ? myCharId.getValue().toString() : null,
                myCharacterName,
                myCharacterImageUrl,
                room.getUnreadCount(currentUserId),
                lastMessageContent,
                room.getLastMessageAt(),
                room.getCreatedAt()
        );
    }
}
