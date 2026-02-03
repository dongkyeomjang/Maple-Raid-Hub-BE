package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.in.web.dto.auth.LoginRequest;
import com.mapleraid.adapter.in.web.dto.auth.SignupRequest;
import com.mapleraid.adapter.in.web.dto.auth.UserResponse;
import com.mapleraid.adapter.in.web.security.CurrentUser;
import com.mapleraid.application.service.AuthService;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.CookieUtil;
import com.mapleraid.core.utility.HttpServletUtil;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import com.mapleraid.security.application.dto.DefaultJsonWebTokenDto;
import com.mapleraid.security.domain.type.ESecurityProvider;
import com.mapleraid.security.domain.type.ESecurityRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JsonWebTokenUtil jsonWebTokenUtil;
    private final HttpServletUtil httpServletUtil;

    @Value("${cookie.temporary-token-name:temporary_token}")
    private String temporaryTokenCookieName;

    @PostMapping("/signup")
    public void signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse httpResponse
    ) throws IOException {
        User user = authService.signup(
                request.username(),
                request.password(),
                request.nickname()
        );

        // 회원가입 후 자동 로그인 - 쿠키에 토큰 설정
        DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                user.getId().getValue(),
                ESecurityRole.USER
        );

        httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);
    }

    @PostMapping("/login")
    public void login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) throws IOException {
        User user = authService.login(request.username(), request.password());
        user.recordLogin();

        DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                user.getId().getValue(),
                ESecurityRole.USER
        );

        // 쿠키에 토큰 설정
        httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);
    }

    @PostMapping("/logout")
    public void logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) throws IOException {
        // 쿠키 삭제
        httpServletUtil.onSuccessBodyResponseWithDeletedJWTCookie(httpRequest, httpResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser UserId userId) {
        User user = authService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @CurrentUser UserId userId,
            @Valid @RequestBody UpdateNicknameRequest request) {
        User user = authService.updateProfile(userId, request.nickname());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    /**
     * OAuth 회원가입 완료 - 임시 토큰을 받아서 닉네임 설정 후 정상 토큰 발급
     */
    @PostMapping("/oauth/complete")
    public void completeOAuthSignup(
            @Valid @RequestBody OAuthCompleteRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) throws IOException {
        String temporaryToken = CookieUtil.refineCookie(httpRequest, temporaryTokenCookieName)
                .orElseThrow(() -> new DomainException("AUTH_INVALID_TOKEN",
                        "임시 토큰이 없습니다. 다시 로그인해주세요."));

        try {
            Claims claims = jsonWebTokenUtil.validateToken(temporaryToken);
            String tokenData = claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class);

            // temporaryToken의 aid에는 "providerId:provider" 형식으로 저장됨
            String[] parts = tokenData.split(":");
            if (parts.length < 2) {
                throw new DomainException("AUTH_INVALID_TOKEN", "잘못된 토큰 형식입니다.");
            }

            String providerId = parts[0];
            ESecurityProvider provider = ESecurityProvider.valueOf(parts[1]);

            // OAuth 사용자 생성
            User user = authService.signupOAuth(provider.name().toLowerCase(), providerId, request.nickname());

            // JWT 토큰 발급
            DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                    user.getId().getValue(),
                    ESecurityRole.USER
            );

            // 쿠키에 토큰 설정하고 리다이렉트
            httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);

        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException("AUTH_INVALID_TOKEN", "토큰 처리 중 오류가 발생했습니다.");
        }
    }


    public record UpdateNicknameRequest(
            @jakarta.validation.constraints.NotBlank(message = "닉네임은 필수입니다")
            @jakarta.validation.constraints.Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
            String nickname
    ) {
    }

    public record OAuthCompleteRequest(
            @jakarta.validation.constraints.NotBlank(message = "닉네임은 필수입니다")
            @jakarta.validation.constraints.Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
            String nickname
    ) {
    }
}
