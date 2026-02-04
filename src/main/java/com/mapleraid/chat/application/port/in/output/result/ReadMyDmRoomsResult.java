package com.mapleraid.chat.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadMyDmRoomsResult extends SelfValidating<ReadMyDmRoomsResult> {
    private final List<DmRoomSummary> rooms;

    public ReadMyDmRoomsResult(List<DmRoomSummary> rooms) {
        this.rooms = rooms;
        this.validateSelf();
    }

    @Getter
    public static class DmRoomSummary {
        private final String id;
        private final String postId;
        private final String otherUserId;
        private final String otherUserNickname;
        private final String otherCharacterId;
        private final String otherCharacterName;
        private final String otherCharacterImageUrl;
        private final String myCharacterId;
        private final String myCharacterName;
        private final String myCharacterImageUrl;
        private final int unreadCount;
        private final String lastMessageContent;
        private final Instant lastMessageAt;
        private final Instant createdAt;

        public DmRoomSummary(String id, String postId,
                             String otherUserId, String otherUserNickname,
                             String otherCharacterId, String otherCharacterName, String otherCharacterImageUrl,
                             String myCharacterId, String myCharacterName, String myCharacterImageUrl,
                             int unreadCount, String lastMessageContent,
                             Instant lastMessageAt, Instant createdAt) {
            this.id = id;
            this.postId = postId;
            this.otherUserId = otherUserId;
            this.otherUserNickname = otherUserNickname;
            this.otherCharacterId = otherCharacterId;
            this.otherCharacterName = otherCharacterName;
            this.otherCharacterImageUrl = otherCharacterImageUrl;
            this.myCharacterId = myCharacterId;
            this.myCharacterName = myCharacterName;
            this.myCharacterImageUrl = myCharacterImageUrl;
            this.unreadCount = unreadCount;
            this.lastMessageContent = lastMessageContent;
            this.lastMessageAt = lastMessageAt;
            this.createdAt = createdAt;
        }
    }
}
