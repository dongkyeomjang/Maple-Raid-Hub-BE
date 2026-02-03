package com.mapleraid.adapter.in.websocket.dto;

import java.time.Instant;

public record DmChatMessage(
        String id,
        String roomId,
        String senderId,
        String senderNickname,
        String senderCharacterId,
        String senderCharacterName,
        String senderCharacterImageUrl,
        String content,
        MessageType type,
        Instant timestamp
) {
    public static DmChatMessage text(String roomId, String senderId, String senderNickname,
                                     String senderCharacterId, String senderCharacterName,
                                     String senderCharacterImageUrl, String content) {
        return new DmChatMessage(
                java.util.UUID.randomUUID().toString(),
                roomId,
                senderId,
                senderNickname,
                senderCharacterId,
                senderCharacterName,
                senderCharacterImageUrl,
                content,
                MessageType.TEXT,
                Instant.now()
        );
    }

    public static DmChatMessage system(String roomId, String content) {
        return new DmChatMessage(
                java.util.UUID.randomUUID().toString(),
                roomId,
                null,
                "System",
                null,
                null,
                null,
                content,
                MessageType.SYSTEM,
                Instant.now()
        );
    }

    public enum MessageType {
        TEXT,
        SYSTEM
    }
}
