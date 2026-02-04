package com.mapleraid.chat.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "direct_message_rooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user1_id", "user2_id"}))
public class DirectMessageRoomJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "post_id", length = 36)
    private String postId;

    @Column(name = "user1_id", nullable = false, length = 36)
    private String user1Id;

    @Column(name = "user2_id", nullable = false, length = 36)
    private String user2Id;

    @Column(name = "user1_character_id", length = 36)
    private String user1CharacterId;

    @Column(name = "user2_character_id", length = 36)
    private String user2CharacterId;

    @Column(name = "unread_count_user1")
    private int unreadCountUser1;

    @Column(name = "unread_count_user2")
    private int unreadCountUser2;

    @Column(name = "last_message_content", length = 500)
    private String lastMessageContent;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static DirectMessageRoomJpaEntity fromDomain(DirectMessageRoom room) {
        DirectMessageRoomJpaEntity entity = new DirectMessageRoomJpaEntity();
        entity.id = room.getId().getValue().toString();
        entity.postId = room.getPostId() != null ? room.getPostId().getValue().toString() : null;
        entity.user1Id = room.getUser1Id().getValue().toString();
        entity.user2Id = room.getUser2Id().getValue().toString();
        entity.user1CharacterId = room.getUser1CharacterId() != null ? room.getUser1CharacterId().getValue().toString() : null;
        entity.user2CharacterId = room.getUser2CharacterId() != null ? room.getUser2CharacterId().getValue().toString() : null;
        entity.unreadCountUser1 = room.getUnreadCountUser1();
        entity.unreadCountUser2 = room.getUnreadCountUser2();
        if (room.getLastMessage() != null) {
            entity.lastMessageContent = room.getLastMessage().getContent();
        }
        entity.lastMessageAt = room.getLastMessageAt();
        entity.createdAt = room.getCreatedAt();
        return entity;
    }

    public DirectMessageRoom toDomain() {
        DirectMessage lastMsg = null;
        // lastMessage는 간략 정보만 복원 (목록 표시용)

        return DirectMessageRoom.reconstitute(
                DirectMessageRoomId.of(id),
                postId != null ? PostId.of(postId) : null,
                UserId.of(user1Id),
                UserId.of(user2Id),
                user1CharacterId != null ? CharacterId.of(user1CharacterId) : null,
                user2CharacterId != null ? CharacterId.of(user2CharacterId) : null,
                unreadCountUser1,
                unreadCountUser2,
                lastMsg,
                lastMessageAt,
                createdAt
        );
    }
}
