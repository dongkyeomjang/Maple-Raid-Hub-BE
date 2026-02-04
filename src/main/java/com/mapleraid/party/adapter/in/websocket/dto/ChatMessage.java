package com.mapleraid.party.adapter.in.websocket.dto;

import java.time.Instant;

public record ChatMessage(
        String id,
        String partyRoomId,
        String senderId,
        String senderNickname,
        String content,
        MessageType type,
        Instant timestamp
) {
    public static ChatMessage chat(String partyRoomId, String senderId, String senderNickname, String content) {
        return new ChatMessage(
                java.util.UUID.randomUUID().toString(),
                partyRoomId,
                senderId,
                senderNickname,
                content,
                MessageType.CHAT,
                Instant.now()
        );
    }

    public static ChatMessage system(String partyRoomId, String content) {
        return new ChatMessage(
                java.util.UUID.randomUUID().toString(),
                partyRoomId,
                null,
                "System",
                content,
                MessageType.SYSTEM,
                Instant.now()
        );
    }

    public static ChatMessage event(String partyRoomId, String content, MessageType type) {
        return new ChatMessage(
                java.util.UUID.randomUUID().toString(),
                partyRoomId,
                null,
                null,
                content,
                type,
                Instant.now()
        );
    }

    public enum MessageType {
        CHAT,           // 일반 채팅
        JOIN,           // 파티 참가 알림
        LEAVE,          // 파티 탈퇴 알림
        READY,          // 레디 알림
        READY_CHECK,    // 레디 체크 시작
        ALL_READY,      // 전원 레디 완료
        SYSTEM          // 시스템 메시지
    }
}
