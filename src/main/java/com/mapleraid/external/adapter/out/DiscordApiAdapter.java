package com.mapleraid.external.adapter.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.notification.application.port.out.DiscordApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordApiAdapter implements DiscordApiPort {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${discord.oauth2.client-id}")
    private String clientId;

    @Value("${discord.oauth2.client-secret}")
    private String clientSecret;

    @Value("${discord.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.guild-id}")
    private String guildId;

    @Override
    public DiscordTokenResponse exchangeCode(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    DISCORD_API_BASE + "/oauth2/token", request, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return new DiscordTokenResponse(
                    json.get("access_token").asText(),
                    json.has("refresh_token") ? json.get("refresh_token").asText() : null,
                    json.get("token_type").asText()
            );
        } catch (Exception e) {
            log.error("[Discord] OAuth 토큰 교환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Discord OAuth 토큰 교환 실패", e);
        }
    }

    @Override
    public DiscordUserInfo getCurrentUser(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    DISCORD_API_BASE + "/users/@me", HttpMethod.GET, request, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return new DiscordUserInfo(
                    json.get("id").asText(),
                    json.get("username").asText(),
                    json.has("global_name") && !json.get("global_name").isNull()
                            ? json.get("global_name").asText() : json.get("username").asText()
            );
        } catch (Exception e) {
            log.error("[Discord] 유저 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Discord 유저 정보 조회 실패", e);
        }
    }

    @Override
    public boolean addGuildMember(String discordUserId, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bot " + botToken);

            Map<String, String> body = Map.of("access_token", accessToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.exchange(
                    DISCORD_API_BASE + "/guilds/" + guildId + "/members/" + discordUserId,
                    HttpMethod.PUT, request, String.class);

            return true;
        } catch (RestClientException e) {
            // 204 (already member) or 201 (added) 모두 성공
            if (e.getMessage() != null && e.getMessage().contains("204")) {
                return true;
            }
            log.warn("[Discord] 서버 가입 실패 userId={}: {}", discordUserId, e.getMessage());
            return false;
        }
    }
}
