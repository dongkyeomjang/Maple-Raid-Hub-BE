package com.mapleraid.security.filter;

import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.CookieUtil;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import com.mapleraid.security.domain.type.ESecurityRole;
import com.mapleraid.security.info.CustomUserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JsonWebTokenAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JsonWebTokenUtil jsonWebTokenUtil;
    private final String cookieDomain;
    private final String accessTokenCookieName;
    private final String refreshTokenCookieName;
    private final Long accessTokenMaxAge;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 쿠키에서 토큰 확인 (새 방식)
        Optional<String> accessTokenOptional = CookieUtil.refineCookie(request, accessTokenCookieName);

        // 2. 쿠키에 없으면 Authorization 헤더 확인 (하위 호환 - 캐시된 프론트엔드 지원)
        if (accessTokenOptional.isEmpty()) {
            String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
                accessTokenOptional = Optional.of(authHeader.substring(Constants.BEARER_PREFIX.length()));
            }
        }

        if (accessTokenOptional.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = accessTokenOptional.get();
            Claims claims = jsonWebTokenUtil.validateToken(accessToken);
            setAuthentication(request, claims);
            filterChain.doFilter(request, response);

        } catch (DomainException e) {
            // Access token 만료 시 자동 갱신 시도
            if ("TOKEN_EXPIRED".equals(e.getErrorCode())) {
                if (tryRefreshToken(request, response)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            // 갱신 실패 또는 다른 토큰 오류: access token 삭제
            CookieUtil.deleteCookie(request, response, cookieDomain, accessTokenCookieName);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 예상치 못한 오류: access token 삭제
            CookieUtil.deleteCookie(request, response, cookieDomain, accessTokenCookieName);
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Refresh token으로 access token 자동 갱신 시도
     * @return 갱신 성공 여부
     */
    private boolean tryRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshTokenOptional = CookieUtil.refineCookie(request, refreshTokenCookieName);
        if (refreshTokenOptional.isEmpty()) {
            return false;
        }

        try {
            String refreshToken = refreshTokenOptional.get();
            Claims refreshClaims = jsonWebTokenUtil.validateToken(refreshToken);
            UUID accountId = UUID.fromString(refreshClaims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class));

            User user = userRepository.findById(UserId.of(accountId.toString())).orElse(null);
            if (user == null || !user.isActive()) {
                return false;
            }

            // 새 access token 발급 (현재 시스템은 모든 사용자가 USER role)
            String newAccessToken = jsonWebTokenUtil.generateAccessToken(accountId, ESecurityRole.USER);

            // 쿠키에 새 access token 설정
            CookieUtil.addSecureCookie(response, cookieDomain, accessTokenCookieName, newAccessToken,
                    (int) (accessTokenMaxAge / 1000));

            // 인증 설정
            setAuthentication(request, jsonWebTokenUtil.validateToken(newAccessToken));
            log.debug("Access token auto-refreshed for user: {}", accountId);
            return true;

        } catch (Exception e) {
            log.debug("Failed to refresh token: {}", e.getMessage());
            // Refresh token도 유효하지 않으면 둘 다 삭제
            CookieUtil.deleteCookie(request, response, cookieDomain, accessTokenCookieName);
            CookieUtil.deleteCookie(request, response, cookieDomain, refreshTokenCookieName);
            return false;
        }
    }

    /**
     * SecurityContext에 인증 정보 설정
     */
    private void setAuthentication(HttpServletRequest request, Claims claims) {
        UUID accountId = UUID.fromString(claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class));
        User user = userRepository.findById(UserId.of(accountId.toString())).orElse(null);

        if (user != null && user.isActive()) {
            CustomUserPrincipal principal = CustomUserPrincipal.create(user);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 필터는 모든 요청에서 실행
        // 토큰이 없으면 그냥 통과하고, 있으면 인증 설정
        // Spring Security가 권한 검사를 수행
        return false;
    }
}
