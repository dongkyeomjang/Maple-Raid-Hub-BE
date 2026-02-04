package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.ReadPartyChatMessagesResult;

import java.time.Instant;
import java.util.List;

public record PartyChatMessagesPageResponseDto(
        List<ChatMessageDto> messages,
        boolean hasMore,
        String nextCursor
) {
    public static PartyChatMessagesPageResponseDto from(ReadPartyChatMessagesResult result) {
        List<ChatMessageDto> msgs = result.getMessages().stream()
                .map(m -> new ChatMessageDto(m.getId(), m.getPartyRoomId(), m.getSenderId(), m.getSenderNickname(),
                        m.getContent(), m.getType(), m.getTimestamp()))
                .toList();
        return new PartyChatMessagesPageResponseDto(msgs, result.isHasMore(), result.getNextCursor());
    }

    public record ChatMessageDto(String id, String partyRoomId, String senderId, String senderNickname,
                                 String content, String type, Instant timestamp) {
    }
}
