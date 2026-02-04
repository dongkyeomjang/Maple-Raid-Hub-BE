package com.mapleraid.chat.application.port.in.input.command;

import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class MarkDmAsReadInput {
    private final DirectMessageRoomId roomId;
    private final UserId userId;

    private MarkDmAsReadInput(DirectMessageRoomId roomId, UserId userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    public static MarkDmAsReadInput of(DirectMessageRoomId roomId, UserId userId) {
        return new MarkDmAsReadInput(roomId, userId);
    }
}
