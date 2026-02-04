package com.mapleraid.chat.adapter.in.web.dto.request;

public record SendDmRequestDto(
        String content,
        String senderCharacterId
) {
}
