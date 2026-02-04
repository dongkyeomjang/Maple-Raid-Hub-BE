package com.mapleraid.party.application.port.out;

import java.time.Instant;
import java.util.List;

public interface PartyChatMessageRepository {

    void savePartyChatMessage(String roomId, String senderId, String senderNickname, String content, String messageType);

    PartyChatMessagesPage findPartyChatMessages(String roomId, int limit, Instant before);

    String getLastMessageContent(String roomId);

    LastMessageInfo getLastMessageInfo(String roomId);

    record PartyChatMessageDto(String id, String partyRoomId, String senderId, String senderNickname,
                               String content, String type, Instant timestamp) {
    }

    record PartyChatMessagesPage(List<PartyChatMessageDto> messages, boolean hasMore, String nextCursor) {
    }

    record LastMessageInfo(String content, Instant timestamp) {
    }
}
