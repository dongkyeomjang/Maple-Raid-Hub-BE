package com.mapleraid.security.handler.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    // 예상 가능한 OAuth2 에러 코드 (스택트레이스 없이 WARN으로 로깅)
    private static final Set<String> EXPECTED_OAUTH2_ERRORS = Set.of(
            "authorization_request_not_found",
            "invalid_token_response",
            "invalid_user_info_response",
            "access_denied"
    );

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorCode = extractErrorCode(exception);

        if (EXPECTED_OAUTH2_ERRORS.contains(errorCode)) {
            // 예상 가능한 에러는 WARN 레벨로 스택트레이스 없이 로깅
            log.warn("OAuth2 authentication failed: {}", exception.getMessage());
        } else {
            // 예상치 못한 에러는 ERROR 레벨로 스택트레이스 포함 로깅
            log.error("OAuth2 authentication failed unexpectedly: {}", exception.getMessage(), exception);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "OAUTH2_FAILURE");
        error.put("message", "OAuth2 인증에 실패했습니다: " + exception.getMessage());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("data", null);
        result.put("error", error);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String extractErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            return oauth2Exception.getError().getErrorCode();
        }
        return "";
    }
}
