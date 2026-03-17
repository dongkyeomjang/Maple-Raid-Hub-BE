package com.mapleraid.security.adapter.in.web.dto.request;

public record CreateRecoveryChallengeRequestDto(
        String characterName,
        String worldName
) {
}
