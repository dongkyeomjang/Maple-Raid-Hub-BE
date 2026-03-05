package com.mapleraid.notification.application.event;

import com.mapleraid.user.domain.UserId;

public record PartyChatMessageReceivedEvent(
        UserId recipientId,
        String partyRoomId,
        String senderNickname,
        String messagePreview
) {}
