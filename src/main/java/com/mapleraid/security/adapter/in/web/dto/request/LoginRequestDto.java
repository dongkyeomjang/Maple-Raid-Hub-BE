package com.mapleraid.security.adapter.in.web.dto.request;

public record LoginRequestDto(
        String username,
        String password
) {
}
