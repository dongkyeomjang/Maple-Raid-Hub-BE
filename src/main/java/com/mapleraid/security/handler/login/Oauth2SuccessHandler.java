package com.mapleraid.security.handler.login;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.utility.HttpServletUtil;
import com.mapleraid.security.application.dto.OauthJsonWebTokenDto;
import com.mapleraid.security.application.port.in.usecase.LoginOauthUseCase;
import com.mapleraid.security.info.CustomTemporaryUserPrincipal;
import com.mapleraid.security.info.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final LoginOauthUseCase loginOauthUseCase;
    private final HttpServletUtil httpServletUtil;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        OauthJsonWebTokenDto oauthJsonWebTokenDto;

        if (principal instanceof CustomTemporaryUserPrincipal) {
            // 신규 사용자: 임시 토큰 발급 후 추가 정보 입력 페이지로 리다이렉트
            oauthJsonWebTokenDto = loginOauthUseCase.execute((CustomTemporaryUserPrincipal) principal);
            httpServletUtil.onSuccessRedirectResponseWithJWTCookie(
                    Constants.ADDITIONAL_INFO_INPUT_PATH + "?oauth=success",
                    response,
                    oauthJsonWebTokenDto
            );
        } else if (principal instanceof CustomUserPrincipal) {
            // 기존 사용자: access + refresh 토큰 발급 후 메인 페이지로 리다이렉트
            oauthJsonWebTokenDto = loginOauthUseCase.execute((CustomUserPrincipal) principal);
            httpServletUtil.onSuccessRedirectResponseWithJWTCookie(
                    "posts?oauth=success",
                    response,
                    oauthJsonWebTokenDto
            );
        } else {
            throw new RuntimeException("Invalid principal type");
        }
    }
}
