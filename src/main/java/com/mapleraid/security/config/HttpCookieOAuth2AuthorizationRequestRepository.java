package com.mapleraid.security.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;

/**
 * Stateless 세션 환경에서 OAuth2 인증 요청을 쿠키에 저장하는 저장소.
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

    private final String cookieDomain;

    public HttpCookieOAuth2AuthorizationRequestRepository(
            @Value("${cookie.domain:localhost}") String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeCookie(response);
            return;
        }

        String serialized = Base64.getEncoder().encodeToString(
                SerializationUtils.serialize(authorizationRequest));

        Cookie cookie = new Cookie(COOKIE_NAME, serialized);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        cookie.setDomain(cookieDomain);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = getCookie(request);
        if (authRequest != null) {
            removeCookie(response);
        }
        return authRequest;
    }

    private OAuth2AuthorizationRequest getCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(cookie -> {
                    try {
                        byte[] bytes = Base64.getDecoder().decode(cookie.getValue());
                        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain(cookieDomain);
        response.addCookie(cookie);
    }
}
