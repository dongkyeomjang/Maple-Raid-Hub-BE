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

    public SignupResult(
            String userId,
            String username,
            String nickname,
            boolean nicknameSet,
            double temperature,
            int completedParties,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.nicknameSet = nicknameSet;
        this.temperature = temperature;
        this.completedParties = completedParties;
        this.createdAt = createdAt;

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
                LocalDateTime.ofInstant(user.getCreatedAt(), ZoneId.systemDefault())
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
