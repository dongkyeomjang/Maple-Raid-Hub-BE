package com.mapleraid.chat.application.port.in.output.result;

import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadDmMessagesResult extends SelfValidating<ReadDmMessagesResult> {
    private final List<DmMessageItem> messages;
    private final boolean hasMore;
    private final String nextCursor;

    public ReadDmMessagesResult(List<DmMessageItem> messages, boolean hasMore, String nextCursor) {
        this.messages = messages;
        this.hasMore = hasMore;
        this.nextCursor = nextCursor;
        this.validateSelf();
    }

    @Getter
    public static class DmMessageItem {
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

        public DmMessageItem(String id, String roomId, String senderId,
                             String senderNickname, String senderCharacterId,
                             String senderCharacterName, String senderCharacterImageUrl,
                             String content, String type, boolean isRead, Instant createdAt) {
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
        }

        public static DmMessageItem from(DirectMessage msg) {
            return new DmMessageItem(
                    msg.getId().getValue().toString(),
                    msg.getRoomId().getValue().toString(),
                    msg.getSenderId() != null ? msg.getSenderId().getValue().toString() : null,
                    null,
                    msg.getSenderCharacterId() != null ? msg.getSenderCharacterId().getValue().toString() : null,
                    null,
                    null,
                    msg.getContent(),
                    msg.getType().name(),
                    msg.isRead(),
                    msg.getCreatedAt()
            );
        }

        public DmMessageItem withSenderInfo(String senderNickname, String senderCharacterName,
                                            String senderCharacterImageUrl) {
            return new DmMessageItem(
                    this.id, this.roomId, this.senderId,
                    senderNickname, this.senderCharacterId,
                    senderCharacterName, senderCharacterImageUrl,
                    this.content, this.type, this.isRead, this.createdAt
            );
        }
    }
}
