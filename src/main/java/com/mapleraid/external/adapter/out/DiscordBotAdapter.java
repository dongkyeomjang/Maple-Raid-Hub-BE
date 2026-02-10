package com.mapleraid.external.adapter.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordBotAdapter implements SendDiscordNotificationPort {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${discord.bot.token}")
    private String botToken;

    @Override
    public NotificationResult sendNotification(String discordUserId, String message) {
        try {
            String channelId = createDmChannel(discordUserId);
            if (channelId == null) {
                return NotificationResult.FAILED;
            }
            return sendMessage(channelId, message);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("[Discord Bot] DM 발송 불가 (403 Forbidden) userId={}", discordUserId);
                return NotificationResult.USER_NOT_REACHABLE;
            }
            log.error("[Discord Bot] DM 발송 실패 userId={}: {}", discordUserId, e.getMessage(), e);
            return NotificationResult.FAILED;
        } catch (Exception e) {
            log.error("[Discord Bot] DM 발송 실패 userId={}: {}", discordUserId, e.getMessage(), e);
            return NotificationResult.FAILED;
        }
    }

    private String createDmChannel(String discordUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bot " + botToken);

        Map<String, String> body = Map.of("recipient_id", discordUserId);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String response = restTemplate.postForObject(
                    DISCORD_API_BASE + "/users/@me/channels", request, String.class);

            JsonNode json = objectMapper.readTree(response);
            return json.get("id").asText();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw e; // 상위 메서드에서 처리
            }
            log.error("[Discord Bot] DM 채널 생성 실패 userId={}: {}", discordUserId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("[Discord Bot] DM 채널 생성 실패 userId={}: {}", discordUserId, e.getMessage());
            return null;
        }
    }

    private NotificationResult sendMessage(String channelId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bot " + botToken);

        Map<String, String> body = Map.of("content", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(
                    DISCORD_API_BASE + "/channels/" + channelId + "/messages", request, String.class);

            return NotificationResult.SUCCESS;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw e; // 상위 메서드에서 처리
            }
            log.error("[Discord Bot] 메시지 전송 실패 channelId={}: {}", channelId, e.getMessage());
            return NotificationResult.FAILED;
        } catch (Exception e) {
            log.error("[Discord Bot] 메시지 전송 실패 channelId={}: {}", channelId, e.getMessage());
            return NotificationResult.FAILED;
        }
    }
}
