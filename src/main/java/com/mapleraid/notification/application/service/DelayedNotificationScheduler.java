package com.mapleraid.notification.application.service;

import com.mapleraid.notification.application.event.DmMessageReceivedEvent;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort.NotificationResult;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DelayedNotificationScheduler {

    private static final String PENDING_KEY = "notification:pending";
    private static final long DELAY_SECONDS = 300; // 5분

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final DirectMessageRoomRepository dmRoomRepository;
    private final SendDiscordNotificationPort discordNotificationPort;

    @Async("notificationExecutor")
    @EventListener
    public void handleDmMessageReceived(DmMessageReceivedEvent event) {
        try {
            String member = event.recipientId().getValue().toString() + ":" + event.dmRoomId();
            double score = Instant.now().plusSeconds(DELAY_SECONDS).toEpochMilli();

            // ZADD: 같은 member면 score만 갱신 (디바운싱)
            redisTemplate.opsForZSet().add(PENDING_KEY, member, score);

            // 이벤트의 메시지 정보를 별도 키에 저장 (최신 메시지 미리보기)
            String previewKey = "notification:preview:" + member;
            String previewValue = event.senderNickname() + "|" + event.messagePreview();
            redisTemplate.opsForValue().set(previewKey, previewValue);
        } catch (Exception e) {
            log.error("[알림] DM 알림 등록 실패: {}", e.getMessage(), e);
        }
    }

    public void cancelPendingNotification(UserId userId, String dmRoomId) {
        try {
            String member = userId.getValue().toString() + ":" + dmRoomId;
            redisTemplate.opsForZSet().remove(PENDING_KEY, member);
            redisTemplate.delete("notification:preview:" + member);
        } catch (Exception e) {
            log.error("[알림] DM 알림 취소 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 30000) // 30초마다
    public void processExpiredNotifications() {
        try {
            double now = Instant.now().toEpochMilli();
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            Set<String> expired = zSetOps.rangeByScore(PENDING_KEY, 0, now);
            if (expired == null || expired.isEmpty()) {
                return;
            }

            for (String member : expired) {
                try {
                    processSingleNotification(member);
                } catch (Exception e) {
                    log.error("[알림] DM 알림 처리 실패 member={}: {}", member, e.getMessage(), e);
                } finally {
                    zSetOps.remove(PENDING_KEY, member);
                    redisTemplate.delete("notification:preview:" + member);
                }
            }
        } catch (Exception e) {
            log.error("[알림] 스케줄러 실행 실패: {}", e.getMessage(), e);
        }
    }

    private void processSingleNotification(String member) {
        String[] parts = member.split(":", 2);
        if (parts.length != 2) return;

        UserId userId = UserId.of(parts[0]);
        String dmRoomId = parts[1];

        // 아직 안읽음인지 확인
        DirectMessageRoom room = dmRoomRepository.findById(DirectMessageRoomId.of(dmRoomId))
                .orElse(null);
        if (room == null || room.getUnreadCount(userId) == 0) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isDiscordLinked() || !user.isDiscordNotificationsEnabled()) {
            return;
        }

        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(NotificationPreference.createDefault(userId));
        if (!pref.isNotifyDmReceived()) {
            return;
        }

        // 미리보기 데이터 조회
        String previewKey = "notification:preview:" + member;
        String previewValue = redisTemplate.opsForValue().get(previewKey);

        String senderNickname = "알 수 없는 사용자";
        String messagePreview = "새 메시지가 있습니다.";

        if (previewValue != null) {
            String[] previewParts = previewValue.split("\\|", 2);
            if (previewParts.length == 2) {
                senderNickname = previewParts[0];
                messagePreview = previewParts[1];
                if (messagePreview.length() > 50) {
                    messagePreview = messagePreview.substring(0, 50) + "...";
                }
            }
        }

        String message = String.format(
                "[DM 알림] %s님의 새 메시지: \"%s\"\n→ https://www.mapleraid.com/me",
                senderNickname, messagePreview);

        NotificationResult result = discordNotificationPort.sendNotification(user.getDiscordId(), message);

        if (result == NotificationResult.USER_NOT_REACHABLE) {
            log.warn("[알림] 유저 DM 전송 불가, 디스코드 연동 해제 userId={}", userId.getValue());
            user.unlinkDiscord();
            userRepository.save(user);
        }
    }
}
