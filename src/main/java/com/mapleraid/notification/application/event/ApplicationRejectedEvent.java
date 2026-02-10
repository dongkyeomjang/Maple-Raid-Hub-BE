package com.mapleraid.notification.application.event;

import com.mapleraid.user.domain.UserId;

public record ApplicationRejectedEvent(
        UserId applicantId,
        String bossName
) {}
