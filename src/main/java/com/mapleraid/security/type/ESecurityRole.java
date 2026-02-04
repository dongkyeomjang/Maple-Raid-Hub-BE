package com.mapleraid.security.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ESecurityRole {

    GUEST("게스트", "ROLE_GUEST"),
    USER("사용자", "ROLE_USER"),
    ADMIN("관리자", "ROLE_ADMIN");

    private final String koName;
    private final String securityName;

    public static ESecurityRole fromString(String value) {
        return switch (value.toUpperCase()) {
            case "GUEST", "ROLE_GUEST" -> GUEST;
            case "USER", "ROLE_USER" -> USER;
            case "ADMIN", "ROLE_ADMIN" -> ADMIN;
            default -> throw new IllegalArgumentException("잘못된 Role입니다: " + value);
        };
    }
}
