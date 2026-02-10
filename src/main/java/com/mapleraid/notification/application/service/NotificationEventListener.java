package com.mapleraid.notification.application.service;

import com.mapleraid.core.utility.BossNameResolver;
import com.mapleraid.notification.application.event.ApplicationAcceptedEvent;
import com.mapleraid.notification.application.event.ApplicationReceivedEvent;
import com.mapleraid.notification.application.event.ApplicationRejectedEvent;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort.NotificationResult;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final SendDiscordNotificationPort discordNotificationPort;
    private final BossNameResolver bossNameResolver;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleApplicationReceived(ApplicationReceivedEvent event) {
        String bossDisplayName = bossNameResolver.resolve(event.bossName());
        sendIfEnabled(event.postAuthorId(), pref -> pref.isNotifyApplicationReceived(),
                String.format("[지원 알림] %s님이 '%s' 파티에 지원했습니다.\n→ https://www.mapleraid.com/posts/%s",
                        event.applicantNickname(), bossDisplayName, event.postId()));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleApplicationAccepted(ApplicationAcceptedEvent event) {
        String bossDisplayName = bossNameResolver.resolve(event.bossName());
        sendIfEnabled(event.applicantId(), pref -> pref.isNotifyApplicationAccepted(),
                String.format("[수락 알림] '%s' 파티 지원이 수락되었습니다!\n→ https://www.mapleraid.com/chat/%s",
                        bossDisplayName, event.partyRoomId()));
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleApplicationRejected(ApplicationRejectedEvent event) {
        String bossDisplayName = bossNameResolver.resolve(event.bossName());
        sendIfEnabled(event.applicantId(), pref -> pref.isNotifyApplicationRejected(),
                String.format("[거절 알림] '%s' 파티 지원이 거절되었습니다.", bossDisplayName));
    }

    private void sendIfEnabled(UserId userId,
                                java.util.function.Predicate<NotificationPreference> prefCheck,
                                String message) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !user.isDiscordLinked() || !user.isDiscordNotificationsEnabled()) {
                return;
            }

            NotificationPreference pref = preferenceRepository.findByUserId(userId)
                    .orElse(NotificationPreference.createDefault(userId));

            if (!prefCheck.test(pref)) {
                return;
            }

            NotificationResult result = discordNotificationPort.sendNotification(user.getDiscordId(), message);

            if (result == NotificationResult.USER_NOT_REACHABLE) {
                log.warn("[알림] 유저 DM 전송 불가, 디스코드 연동 해제 userId={}", userId.getValue());
                user.unlinkDiscord();
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("[알림] Discord DM 발송 실패 userId={}: {}", userId.getValue(), e.getMessage(), e);
        }
    }
}
