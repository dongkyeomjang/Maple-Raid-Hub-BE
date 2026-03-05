package com.mapleraid.core.constant;

import java.util.List;

public class Constants {

    public static final int MIN_CHARACTER_LEVEL = 260;

    public static final int DAILY_CHALLENGE_LIMIT = 5;

    // JWT
    public static String ACCOUNT_ID_CLAIM_NAME = "aid";
    public static String ACCOUNT_ROLE_CLAIM_NAME = "rol";

    // HEADER
    public static String BEARER_PREFIX = "Bearer ";
    public static String AUTHORIZATION_HEADER = "Authorization";

    // Additional Info Input Url
    public static String ADDITIONAL_INFO_INPUT_PATH = "oauth/set-nickname";

    /**
     * 인증이 필요 없는 URL (HTTP 메서드 무관)
     */
    public static List<String> NO_NEED_AUTH_URLS = List.of(
            // OAuth2
            "/oauth2/authorization/kakao",
            "/login/oauth2/code/kakao",

            // Auth
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/oauth/complete",

            // Public APIs
            "/api/config/**",
            "/api/stats",

            // WebSocket
            "/ws/**",

            // Health Check
            "/actuator/health"
    );

    /**
     * GET 요청만 인증 없이 허용되는 URL
     */
    public static List<String> NO_NEED_AUTH_GET_URLS = List.of(
            "/api/posts",
            "/api/posts/*",
            "/api/posts/*/applications"
    );
}
