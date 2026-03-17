package com.mapleraid.security.adapter.in.web.dto.request;

public record ResetPasswordRequestDto(
        String recoveryToken,
        String newPassword
) {
}
