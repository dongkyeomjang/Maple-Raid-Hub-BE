package com.mapleraid.security.adapter.in.web.dto.request;

public record SignupRequestDto(
        String username,
        String password,
        String nickname
) {
}
