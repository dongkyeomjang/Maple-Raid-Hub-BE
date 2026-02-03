package com.mapleraid.adapter.in.websocket.config;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JsonWebTokenUtil jsonWebTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String accountId = null;

            // 1. 핸드셰이크에서 저장한 accountId 확인 (쿠키 기반 - 새 방식)
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                accountId = (String) sessionAttributes.get("accountId");
            }

            // 2. 쿠키에 없으면 Authorization 헤더 확인 (하위 호환 - 캐시된 프론트엔드 지원)
            if (accountId == null) {
                List<String> authHeaders = accessor.getNativeHeader("Authorization");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.get(0);
                    if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
                        String token = authHeader.substring(Constants.BEARER_PREFIX.length());
                        try {
                            Claims claims = jsonWebTokenUtil.validateToken(token);
                            accountId = claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class);
                        } catch (Exception e) {
                            // 토큰이 유효하지 않은 경우 무시
                        }
                    }
                }
            }

            if (accountId != null) {
                accessor.setUser(new StompPrincipal(accountId));
            }
        }

        return message;
    }

    /**
     * Simple Principal implementation for STOMP
     */
    private static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
