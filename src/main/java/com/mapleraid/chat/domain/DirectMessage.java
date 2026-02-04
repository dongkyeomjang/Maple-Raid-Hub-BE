package com.mapleraid.chat.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.user.domain.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * 1:1 DM 메시지 Entity
 */
public class DirectMessage {

    private final DirectMessageId id;
    private final DirectMessageRoomId roomId;
    private final UserId senderId;
    private final CharacterId senderCharacterId;
    private final String content;
    private final MessageType type;
    private final Instant createdAt;
    private boolean isRead;

    private DirectMessage(DirectMessageId id, DirectMessageRoomId roomId,
                          UserId senderId, CharacterId senderCharacterId,
                          String content, MessageType type) {
        this.id = Objects.requireNonNull(id);
        this.roomId = Objects.requireNonNull(roomId);
        this.senderId = senderId; // SYSTEM 메시지는 null
        this.senderCharacterId = senderCharacterId; // nullable
        this.content = Objects.requireNonNull(content);
        this.type = type;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public static DirectMessage createText(DirectMessageRoomId roomId, UserId senderId,
                                           CharacterId senderCharacterId, String content) {
        return new DirectMessage(
                DirectMessageId.generate(),
                roomId,
                senderId,
                senderCharacterId,
                content,
                MessageType.TEXT
        );
    }

    public static DirectMessage createSystem(DirectMessageRoomId roomId, String content) {
        return new DirectMessage(
                DirectMessageId.generate(),
                roomId,
                null,
                null,
                content,
                MessageType.SYSTEM
        );
    }

    public static DirectMessage reconstitute(
            DirectMessageId id, DirectMessageRoomId roomId, UserId senderId,
            CharacterId senderCharacterId, String content, MessageType type,
            boolean isRead, Instant createdAt) {
        return new DirectMessage(id, roomId, senderId, senderCharacterId, content, type) {
            @Override
            public boolean isRead() {
                return isRead;
            }

            @Override
            public Instant getCreatedAt() {
                return createdAt;
            }
        };
    }

    public void markAsRead() {
        this.isRead = true;
    }

    // Getters
    public DirectMessageId getId() {
        return id;
    }

    public DirectMessageRoomId getRoomId() {
        return roomId;
    }

    public UserId getSenderId() {
        return senderId;
    }

    public CharacterId getSenderCharacterId() {
        return senderCharacterId;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum MessageType {
        TEXT,    // 일반 텍스트
        SYSTEM   // 시스템 메시지
    }
}
