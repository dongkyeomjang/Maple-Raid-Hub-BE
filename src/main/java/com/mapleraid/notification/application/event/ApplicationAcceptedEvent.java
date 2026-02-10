package com.mapleraid.notification.application.event;

import com.mapleraid.user.domain.UserId;

public record ApplicationAcceptedEvent(
        UserId applicantId,
        String bossName,
        String partyRoomId
) {}
