package com.mapleraid.party.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadPartyChatMessagesResult extends SelfValidating<ReadPartyChatMessagesResult> {

    private final List<ChatMessageItem> messages;
    private final boolean hasMore;
    private final String nextCursor;

    public ReadPartyChatMessagesResult(List<ChatMessageItem> messages, boolean hasMore, String nextCursor) {
        this.messages = messages;
        this.hasMore = hasMore;
        this.nextCursor = nextCursor;
        this.validateSelf();
    }

    @Getter
    public static class ChatMessageItem {

        private final String id;
        private final String partyRoomId;
        private final String senderId;
        private final String senderNickname;
        private final String content;
        private final String type;
        private final Instant timestamp;

        public ChatMessageItem(String id, String partyRoomId, String senderId, String senderNickname,
                               String content, String type, Instant timestamp) {
            this.id = id;
            this.partyRoomId = partyRoomId;
            this.senderId = senderId;
            this.senderNickname = senderNickname;
            this.content = content;
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
