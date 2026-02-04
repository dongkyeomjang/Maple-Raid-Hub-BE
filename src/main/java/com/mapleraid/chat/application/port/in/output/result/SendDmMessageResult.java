package com.mapleraid.chat.application.port.in.output.result;

import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;

@Getter
public class SendDmMessageResult extends SelfValidating<SendDmMessageResult> {
    private final String id;
    private final String roomId;
    private final String senderId;
    private final String senderNickname;
    private final String senderCharacterId;
    private final String senderCharacterName;
    private final String senderCharacterImageUrl;
    private final String content;
    private final String type;
    private final boolean isRead;
    private final Instant createdAt;
    private final String recipientUserId;

    public SendDmMessageResult(String id, String roomId, String senderId,
                               String senderNickname, String senderCharacterId,
                               String senderCharacterName, String senderCharacterImageUrl,
                               String content, String type, boolean isRead, Instant createdAt,
                               String recipientUserId) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.senderCharacterId = senderCharacterId;
        this.senderCharacterName = senderCharacterName;
        this.senderCharacterImageUrl = senderCharacterImageUrl;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.recipientUserId = recipientUserId;
        this.validateSelf();
    }

    public static SendDmMessageResult from(DirectMessage message, String senderNickname,
                                           String senderCharacterName, String senderCharacterImageUrl,
                                           String recipientUserId) {
        return new SendDmMessageResult(
                message.getId().getValue().toString(),
                message.getRoomId().getValue().toString(),
                message.getSenderId() != null ? message.getSenderId().getValue().toString() : null,
                senderNickname,
                message.getSenderCharacterId() != null ? message.getSenderCharacterId().getValue().toString() : null,
                senderCharacterName,
                senderCharacterImageUrl,
                message.getContent(),
                message.getType().name(),
                message.isRead(),
                message.getCreatedAt(),
                recipientUserId
        );
    }
}
