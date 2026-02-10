package com.mapleraid.notification.application.event;

import com.mapleraid.user.domain.UserId;

public record ApplicationReceivedEvent(
        UserId postAuthorId,
        String applicantNickname,
        String bossName,
        String postId
) {}
