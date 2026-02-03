package com.mapleraid.adapter.in.web.dto.auth;

import com.mapleraid.domain.user.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String username,
        String nickname,
        boolean nicknameSet,
        double temperature,
        int completedParties,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId().getValue().toString(),
                getDisplayUsername(user),
                user.getNickname(),
                user.isNicknameSet(),
                user.getTemperature(),
                user.getCompletedParties(),
                user.getCreatedAt()
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
