package com.mapleraid.chat.adapter.in.web.dto.response;

import com.mapleraid.chat.application.port.in.output.result.ReadDmMessagesResult;

import java.util.List;

public record DmMessagesPageResponseDto(
        List<DmMessageResponseDto> messages,
        boolean hasMore,
        String nextCursor
) {
    public static DmMessagesPageResponseDto from(ReadDmMessagesResult result) {
        List<DmMessageResponseDto> messages = result.getMessages().stream()
                .map(DmMessageResponseDto::from)
                .toList();
        return new DmMessagesPageResponseDto(messages, result.isHasMore(), result.getNextCursor());
    }
}
