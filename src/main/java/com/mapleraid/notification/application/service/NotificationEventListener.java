package com.mapleraid.notification.application.service;

import com.mapleraid.core.security.AdminActionSigner;
import com.mapleraid.core.utility.BossNameResolver;
import com.mapleraid.notification.application.event.ApplicationAcceptedEvent;
import com.mapleraid.notification.application.event.ApplicationReceivedEvent;
import com.mapleraid.notification.application.event.ApplicationRejectedEvent;
import com.mapleraid.notification.application.event.GuestPostCreatedEvent;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort.LinkButton;
import com.mapleraid.notification.application.port.out.SendDiscordNotificationPort.NotificationResult;
import com.mapleraid.post.adapter.in.web.admin.AdminGuestPostController;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.Collectors;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final SendDiscordNotificationPort discordNotificationPort;
    private final BossNameResolver bossNameResolver;
    private final AdminActionSigner adminActionSigner;

    // 비회원 글 모니터링 알림 수신자 (관리자 Discord User ID). 비어있으면 알림 스킵.
    @Value("${admin.discord-id:}")
    private String adminDiscordId;

    // 링크 버튼이 가리킬 API 도메인 (예: https://api.mapleraid.com). 비어있으면 버튼 생략.
    @Value("${admin.api-base-url:}")
    private String apiBaseUrl;

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

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleGuestPostCreated(GuestPostCreatedEvent event) {
        if (adminDiscordId == null || adminDiscordId.isBlank()) {
            return;
        }

        String bossDisplay = event.bossIds() == null || event.bossIds().isEmpty()
                ? "-"
                : event.bossIds().stream()
                        .map(bossNameResolver::resolve)
                        .collect(Collectors.joining(", "));

        String memoLine = (event.description() != null && !event.description().isBlank())
                ? String.format("%n- 메모: %s", event.description())
                : "";

        String message = String.format(
                "🚨 [비회원 모집글 알림]%n" +
                        "- 캐릭터: %s / %s%n" +
                        "- 보스: %s%n" +
                        "- 연락수단: %s" +
                        "%s%n" +
                        "- 바로가기: https://www.mapleraid.com/posts/%s",
                event.worldName(),
                event.characterName(),
                bossDisplay,
                event.contactLink(),
                memoLine,
                event.postId()
        );

        java.util.List<LinkButton> buttons = buildGuestPostButtons(event.postId());

        try {
            NotificationResult result = discordNotificationPort.sendNotification(adminDiscordId, message, buttons);
            if (result != NotificationResult.SUCCESS) {
                log.warn("[비회원 알림] 관리자 Discord DM 전송 실패 result={} postId={}", result, event.postId());
            }
        } catch (Exception e) {
            log.error("[비회원 알림] Discord DM 발송 예외 postId={}: {}", event.postId(), e.getMessage(), e);
        }
    }

    private java.util.List<LinkButton> buildGuestPostButtons(String postId) {
        if (!adminActionSigner.isEnabled() || apiBaseUrl == null || apiBaseUrl.isBlank()) {
            return java.util.List.of();
        }
        AdminActionSigner.SignedParams signed = adminActionSigner.sign(AdminGuestPostController.ACTION_CANCEL, postId);
        if (signed == null) {
            return java.util.List.of();
        }
        String base = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        String url = String.format("%s/api/admin/guest-posts/%s/cancel?exp=%d&sig=%s",
                base, postId, signed.exp(), signed.sig());
        return java.util.List.of(new LinkButton("🗑️ 글 내리기 (CANCELED)", url));
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
