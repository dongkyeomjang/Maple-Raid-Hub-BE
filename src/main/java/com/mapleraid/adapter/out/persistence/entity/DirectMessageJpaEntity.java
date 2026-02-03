package com.mapleraid.adapter.out.persistence.entity;

import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageId;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "direct_messages",
        indexes = {
                @Index(name = "idx_dm_room_created", columnList = "room_id, created_at DESC")
        })
public class DirectMessageJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "room_id", nullable = false, length = 36)
    private String roomId;

    @Column(name = "sender_id", length = 36)
    private String senderId;

    @Column(name = "sender_character_id", length = 36)
    private String senderCharacterId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DirectMessage.MessageType type;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DirectMessageJpaEntity() {
    }

    public static DirectMessageJpaEntity fromDomain(DirectMessage message) {
        DirectMessageJpaEntity entity = new DirectMessageJpaEntity();
        entity.id = message.getId().getValue().toString();
        entity.roomId = message.getRoomId().getValue().toString();
        entity.senderId = message.getSenderId() != null ? message.getSenderId().getValue().toString() : null;
        entity.senderCharacterId = message.getSenderCharacterId() != null ? message.getSenderCharacterId().getValue().toString() : null;
        entity.content = message.getContent();
        entity.type = message.getType();
        entity.isRead = message.isRead();
        entity.createdAt = message.getCreatedAt();
        return entity;
    }

    public DirectMessage toDomain() {
        return DirectMessage.reconstitute(
                DirectMessageId.of(id),
                DirectMessageRoomId.of(roomId),
                senderId != null ? UserId.of(senderId) : null,
                senderCharacterId != null ? CharacterId.of(senderCharacterId) : null,
                content,
                type,
                isRead,
                createdAt
        );
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public DirectMessage.MessageType getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
