package com.mapleraid.security.adapter.in.web.command;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.core.utility.CookieUtil;
import com.mapleraid.core.utility.HttpServletUtil;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.security.adapter.in.web.dto.request.CompleteOauthSignupRequestDto;
import com.mapleraid.security.adapter.in.web.dto.request.LoginRequestDto;
import com.mapleraid.security.adapter.in.web.dto.request.SignupRequestDto;
import com.mapleraid.security.adapter.in.web.dto.request.UpdateNicknameRequestDto;
import com.mapleraid.security.adapter.in.web.dto.response.UserResponseDto;
import com.mapleraid.security.application.dto.DefaultJsonWebTokenDto;
import com.mapleraid.security.application.port.in.input.command.CompleteOauthSignupInput;
import com.mapleraid.security.application.port.in.input.command.LoginInput;
import com.mapleraid.security.application.port.in.input.command.SignupInput;
import com.mapleraid.security.application.port.in.input.command.UpdateNicknameInput;
import com.mapleraid.security.application.port.in.output.result.CompleteOauthSignupResult;
import com.mapleraid.security.application.port.in.output.result.LoginResult;
import com.mapleraid.security.application.port.in.output.result.SignupResult;
import com.mapleraid.security.application.port.in.usecase.CompleteOauthSignupUseCase;
import com.mapleraid.security.application.port.in.usecase.LoginUseCase;
import com.mapleraid.security.application.port.in.usecase.SignupUseCase;
import com.mapleraid.security.application.port.in.usecase.UpdateNicknameUseCase;
import com.mapleraid.security.type.ESecurityProvider;
import com.mapleraid.security.type.ESecurityRole;
import com.mapleraid.notification.application.port.in.usecase.DismissDiscordPromptUseCase;
import com.mapleraid.user.domain.UserId;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final CompleteOauthSignupUseCase completeOauthSignupUseCase;
    private final UpdateNicknameUseCase updateNicknameUseCase;
    private final DismissDiscordPromptUseCase dismissDiscordPromptUseCase;
    private final JsonWebTokenUtil jsonWebTokenUtil;
    private final HttpServletUtil httpServletUtil;

    @Value("${cookie.temporary-token-name:temporary_token}")
    private String temporaryTokenCookieName;

    @PostMapping("/signup")
    public void signup(
            @RequestBody SignupRequestDto request,
            HttpServletResponse httpResponse
    ) throws IOException {
        SignupResult result = signupUseCase.execute(
                SignupInput.of(
                        request.username(),
                        request.password(),
                        request.nickname()
                )
        );

        DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                UUID.fromString(result.getUserId()),
                ESecurityRole.USER
        );

        httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);
    }

    @PostMapping("/login")
    public void login(
            @RequestBody LoginRequestDto request,
            HttpServletResponse httpResponse
    ) throws IOException {
        LoginResult result = loginUseCase.execute(
                LoginInput.of(
                        request.username(),
                        request.password()
                )
        );

        DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                UUID.fromString(result.getUserId()),
                ESecurityRole.USER
        );

        httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);
    }

    @PostMapping("/logout")
    public void logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) throws IOException {
        httpServletUtil.onSuccessBodyResponseWithDeletedJWTCookie(httpRequest, httpResponse);
    }

    @PostMapping("/oauth/complete")
    public void completeOAuthSignup(
            @RequestBody CompleteOauthSignupRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) throws IOException {
        String temporaryToken = CookieUtil.refineCookie(httpRequest, temporaryTokenCookieName)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_TOKEN_ERROR));

        try {
            Claims claims = jsonWebTokenUtil.validateToken(temporaryToken);
            String tokenData = claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class);

            String[] parts = tokenData.split(":");
            if (parts.length < 2) {
                throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
            }

            String providerId = parts[0];
            ESecurityProvider provider = ESecurityProvider.valueOf(parts[1]);

            CompleteOauthSignupResult result = completeOauthSignupUseCase.execute(
                    CompleteOauthSignupInput.of(
                            providerId,
                            provider,
                            request.nickname()
                    )
            );

            DefaultJsonWebTokenDto tokenDto = jsonWebTokenUtil.generateDefaultJsonWebTokens(
                    UUID.fromString(result.getUserId()),
                    ESecurityRole.USER
            );

            httpServletUtil.onSuccessBodyResponseWithJWTCookie(httpResponse, tokenDto);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }
    }

    @PatchMapping("/discord-prompt-dismiss")
    public ResponseDto<Void> dismissDiscordPrompt(@CurrentUser UserId userId) {
        dismissDiscordPromptUseCase.execute(userId);
        return ResponseDto.ok(null);
    }

    @PatchMapping("/nickname")
    public ResponseDto<UserResponseDto> updateNickname(
            @CurrentUser UserId userId,
            @RequestBody UpdateNicknameRequestDto request) {

        return ResponseDto.ok(
                UserResponseDto.from(
                        updateNicknameUseCase.execute(
                                UpdateNicknameInput.of(
                                        userId,
                                        request.nickname()
                                )
                        )
                )
        );
    }
}
