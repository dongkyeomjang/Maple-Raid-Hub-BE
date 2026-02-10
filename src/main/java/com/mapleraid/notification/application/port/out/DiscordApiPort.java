package com.mapleraid.notification.application.port.out;

public interface DiscordApiPort {

    DiscordTokenResponse exchangeCode(String code);

    DiscordUserInfo getCurrentUser(String accessToken);

    boolean addGuildMember(String discordUserId, String accessToken);

    record DiscordTokenResponse(String accessToken, String refreshToken, String tokenType) {}

    record DiscordUserInfo(String id, String username, String globalName) {}
}
