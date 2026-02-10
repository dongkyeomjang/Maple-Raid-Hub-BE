package com.mapleraid.notification.application.event;

import com.mapleraid.user.domain.UserId;

public record DmMessageReceivedEvent(
        UserId recipientId,
        String dmRoomId,
        String senderNickname,
        String messagePreview
) {}
