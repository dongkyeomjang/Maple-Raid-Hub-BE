package com.mapleraid.adapter.in.web.dto.auth;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new AuthTokenResponse(accessToken, refreshToken, expiresIn, "Bearer");
    }
}
