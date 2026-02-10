package com.mapleraid.notification.application.port.out;

public interface SendDiscordNotificationPort {

    enum NotificationResult {
        SUCCESS,
        USER_NOT_REACHABLE,
        FAILED
    }

    NotificationResult sendNotification(String discordUserId, String message);
}
