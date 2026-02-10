package com.mapleraid.notification.adapter.in.web.dto;

public record DiscordStatusResponse(
        boolean linked,
        String discordUsername
) {}
