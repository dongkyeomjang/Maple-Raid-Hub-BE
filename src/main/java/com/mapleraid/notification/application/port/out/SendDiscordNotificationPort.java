package com.mapleraid.notification.application.port.out;

import java.util.List;

public interface SendDiscordNotificationPort {

    enum NotificationResult {
        SUCCESS,
        USER_NOT_REACHABLE,
        FAILED
    }

    NotificationResult sendNotification(String discordUserId, String message);

    /**
     * Link 버튼을 함께 포함해 DM 전송. components 가 null 이거나 비어있으면 일반 메시지와 동일.
     */
    NotificationResult sendNotification(String discordUserId, String message, List<LinkButton> buttons);

    record LinkButton(String label, String url) {
    }
}
