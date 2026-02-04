package com.mapleraid.chat.application.port.in.output.result;

import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CreateDmRoomResult extends SelfValidating<CreateDmRoomResult> {
    private final String id;
    private final String postId;
    private final String user1Id;
    private final String user2Id;
    private final String user1CharacterId;
    private final String user2CharacterId;
    private final int unreadCountUser1;
    private final int unreadCountUser2;
    private final Instant lastMessageAt;
    private final Instant createdAt;

    public CreateDmRoomResult(String id, String postId, String user1Id, String user2Id,
                              String user1CharacterId, String user2CharacterId,
                              int unreadCountUser1, int unreadCountUser2,
                              Instant lastMessageAt, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.user1CharacterId = user1CharacterId;
        this.user2CharacterId = user2CharacterId;
        this.unreadCountUser1 = unreadCountUser1;
        this.unreadCountUser2 = unreadCountUser2;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
        this.validateSelf();
    }

    public static CreateDmRoomResult from(DirectMessageRoom room) {
        return new CreateDmRoomResult(
                room.getId().getValue().toString(),
                room.getPostId() != null ? room.getPostId().getValue().toString() : null,
                room.getUser1Id().getValue().toString(),
                room.getUser2Id().getValue().toString(),
                room.getUser1CharacterId() != null ? room.getUser1CharacterId().getValue().toString() : null,
                room.getUser2CharacterId() != null ? room.getUser2CharacterId().getValue().toString() : null,
                room.getUnreadCountUser1(),
                room.getUnreadCountUser2(),
                room.getLastMessageAt(),
                room.getCreatedAt()
        );
    }
}
