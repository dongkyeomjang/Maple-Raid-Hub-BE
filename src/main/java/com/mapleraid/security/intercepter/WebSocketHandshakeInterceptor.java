package com.mapleraid.security.intercepter;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.CookieUtil;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JsonWebTokenUtil jsonWebTokenUtil;

    @Value("${cookie.access-token-name:access_token}")
    private String accessTokenCookieName;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // 쿠키에서 access token 읽기
            CookieUtil.refineCookie(httpRequest, accessTokenCookieName).ifPresent(token -> {
                try {
                    Claims claims = jsonWebTokenUtil.validateToken(token);
                    String accountId = claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class);

                    if (accountId != null) {
                        // WebSocket 세션에 사용자 ID 저장
                        attributes.put("accountId", accountId);
                    }
                } catch (Exception e) {
                    // 토큰이 유효하지 않은 경우 무시
                }
            });
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do
    }
}
