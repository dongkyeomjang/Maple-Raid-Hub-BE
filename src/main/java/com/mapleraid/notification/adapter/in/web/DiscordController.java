package com.mapleraid.notification.adapter.in.web;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.notification.adapter.in.web.dto.DiscordStatusResponse;
import com.mapleraid.notification.application.port.in.usecase.GetDiscordStatusUseCase;
import com.mapleraid.notification.application.port.in.usecase.GetDiscordStatusUseCase.DiscordStatus;
import com.mapleraid.notification.application.port.in.usecase.LinkDiscordUseCase;
import com.mapleraid.notification.application.port.in.usecase.UnlinkDiscordUseCase;
import com.mapleraid.user.domain.UserId;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/discord")
@RequiredArgsConstructor
public class DiscordController {

    private static final String DISCORD_OAUTH_STATE_COOKIE = "discord_oauth_state";

    private final GetDiscordStatusUseCase getDiscordStatusUseCase;
    private final LinkDiscordUseCase linkDiscordUseCase;
    private final UnlinkDiscordUseCase unlinkDiscordUseCase;

    @Value("${discord.oauth2.client-id}")
    private String clientId;

    @Value("${discord.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${client.url}")
    private String clientUrl;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @GetMapping("/auth-url")
    public ResponseDto<Map<String, String>> getAuthUrl(HttpServletResponse response) {
        String state = UUID.randomUUID().toString();

        Cookie stateCookie = new Cookie(DISCORD_OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(!cookieDomain.equals("localhost"));
        stateCookie.setPath("/");
        stateCookie.setDomain(cookieDomain);
        stateCookie.setMaxAge(600); // 10분
        response.addCookie(stateCookie);

        String scope = URLEncoder.encode("identify guilds.join", StandardCharsets.UTF_8);
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        String authUrl = "https://discord.com/api/oauth2/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + encodedRedirectUri
                + "&response_type=code"
                + "&scope=" + scope
                + "&state=" + state;

        return ResponseDto.ok(Map.of("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam String state,
            @CurrentUser UserId userId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // state 검증
        String storedState = Arrays.stream(
                        request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(c -> DISCORD_OAUTH_STATE_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (storedState == null || !storedState.equals(state)) {
            response.sendRedirect(clientUrl + "/me/settings?discord=error&reason=state_mismatch");
            return;
        }

        // state 쿠키 제거
        Cookie deleteCookie = new Cookie(DISCORD_OAUTH_STATE_COOKIE, "");
        deleteCookie.setMaxAge(0);
        deleteCookie.setPath("/");
        deleteCookie.setDomain(cookieDomain);
        response.addCookie(deleteCookie);

        try {
            linkDiscordUseCase.execute(userId, code);
            response.sendRedirect(clientUrl + "/me/settings?discord=linked");
        } catch (CommonException e) {
            String errorCode = e.getErrorCode().name().toLowerCase();
            response.sendRedirect(clientUrl + "/me/settings?discord=error&reason=" + errorCode);
        } catch (Exception e) {
            response.sendRedirect(clientUrl + "/me/settings?discord=error&reason=unknown");
        }
    }

    @GetMapping("/status")
    public ResponseDto<DiscordStatusResponse> getStatus(@CurrentUser UserId userId) {
        DiscordStatus status = getDiscordStatusUseCase.execute(userId);
        return ResponseDto.ok(new DiscordStatusResponse(status.linked(), status.discordUsername()));
    }

    @PostMapping("/unlink")
    public ResponseDto<Void> unlink(@CurrentUser UserId userId) {
        unlinkDiscordUseCase.execute(userId);
        return ResponseDto.ok(null);
    }
}
