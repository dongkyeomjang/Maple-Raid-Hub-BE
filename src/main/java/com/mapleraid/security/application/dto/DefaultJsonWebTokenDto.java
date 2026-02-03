package com.mapleraid.security.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultJsonWebTokenDto {
    private final String accessToken;
    private final String refreshToken;
}
