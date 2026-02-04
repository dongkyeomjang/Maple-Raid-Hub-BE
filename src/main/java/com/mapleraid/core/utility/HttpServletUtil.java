package com.mapleraid.core.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.security.application.dto.DefaultJsonWebTokenDto;
import com.mapleraid.security.application.dto.OauthJsonWebTokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HttpServletUtil {

    private final ObjectMapper objectMapper;
    @Value("${client.url}")
    private String clientUrl;
    @Value("${cookie.domain:localhost}")
    private String cookieDomain;
    @Value("${cookie.access-token-name:access_token}")
    private String accessTokenCookieName;
    @Value("${cookie.refresh-token-name:refresh_token}")
    private String refreshTokenCookieName;
    @Value("${cookie.temporary-token-name:temporary_token}")
    private String temporaryTokenCookieName;
    @Value("${jwt.refresh-token-validity-ms}")
    private Long refreshTokenExpirePeriod;

    public void onSuccessRedirectResponseWithJWTCookie(
            String redirectPath,
            HttpServletResponse response,
            OauthJsonWebTokenDto tokenDto
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.CREATED.value());

        // 최초 로그인이 아닐 시
        if (tokenDto.getTemporaryToken() == null) {
            CookieUtil.addSecureCookie(
                    response,
                    cookieDomain,
                    accessTokenCookieName,
                    tokenDto.getAccessToken(),
                    (int) (refreshTokenExpirePeriod / 1000L)
            );

            CookieUtil.addSecureCookie(
                    response,
                    cookieDomain,
                    refreshTokenCookieName,
                    tokenDto.getRefreshToken(),
                    (int) (refreshTokenExpirePeriod / 1000L)
            );
        }
        // 최초 로그인 시 (임시 토큰 발급)
        else {
            CookieUtil.addCookie(
                    response,
                    cookieDomain,
                    temporaryTokenCookieName,
                    tokenDto.getTemporaryToken()
            );
        }

        response.sendRedirect(String.format("%s/%s", clientUrl, redirectPath));
    }

    public void onSuccessBodyResponseWithDeletedJWTCookie(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        CookieUtil.deleteCookie(request, response, cookieDomain, accessTokenCookieName);
        CookieUtil.deleteCookie(request, response, cookieDomain, refreshTokenCookieName);
        CookieUtil.deleteCookie(request, response, cookieDomain, temporaryTokenCookieName);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", null);
        result.put("error", null);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * OAuth 회원가입 완료 후 JWT 쿠키 설정 및 JSON 응답
     */
    public void onSuccessBodyResponseWithJWTCookie(
            HttpServletResponse response,
            DefaultJsonWebTokenDto tokenDto
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.CREATED.value());

        // 임시 토큰 삭제 및 정상 토큰 설정
        CookieUtil.addSecureCookie(
                response,
                cookieDomain,
                accessTokenCookieName,
                tokenDto.getAccessToken(),
                (int) (refreshTokenExpirePeriod / 1000L)
        );

        CookieUtil.addSecureCookie(
                response,
                cookieDomain,
                refreshTokenCookieName,
                tokenDto.getRefreshToken(),
                (int) (refreshTokenExpirePeriod / 1000L)
        );

        // 임시 토큰 삭제
        CookieUtil.deleteCookieByResponse(response, cookieDomain, temporaryTokenCookieName);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", null);
        result.put("error", null);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
