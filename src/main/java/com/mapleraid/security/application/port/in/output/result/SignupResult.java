package com.mapleraid.security.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
public class SignupResult extends SelfValidating<SignupResult> {

    private final String userId;

    private final String username;

    private final String nickname;

    private final boolean nicknameSet;

    private final double temperature;

    private final int completedParties;

    private final LocalDateTime createdAt;

    private final boolean discordLinked;

    private final String discordUsername;

    private final boolean discordPromptDismissed;

    public SignupResult(
            String userId,
            String username,
            String nickname,
            boolean nicknameSet,
            double temperature,
            int completedParties,
            LocalDateTime createdAt,
            boolean discordLinked,
            String discordUsername,
            boolean discordPromptDismissed
    ) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.nicknameSet = nicknameSet;
        this.temperature = temperature;
        this.completedParties = completedParties;
        this.createdAt = createdAt;
        this.discordLinked = discordLinked;
        this.discordUsername = discordUsername;
        this.discordPromptDismissed = discordPromptDismissed;

        this.validateSelf();
    }

    public static SignupResult from(User user) {
        return new SignupResult(
                user.getId().getValue().toString(),
                getDisplayUsername(user),
                user.getNickname(),
                user.isNicknameSet(),
                user.getTemperature(),
                user.getCompletedParties(),
                LocalDateTime.ofInstant(user.getCreatedAt(), ZoneId.systemDefault()),
                user.isDiscordLinked(),
                user.getDiscordUsername(),
                user.isDiscordPromptDismissed()
        );
    }

    private static String getDisplayUsername(User user) {
        if (user.isOAuthUser()) {
            return switch (user.getProvider()) {
                case "kakao" -> "카카오 로그인";
                case "naver" -> "네이버 로그인";
                case "google" -> "구글 로그인";
                default -> user.getProvider() + " 로그인";
            };
        }
        return user.getUsername();
    }
}
