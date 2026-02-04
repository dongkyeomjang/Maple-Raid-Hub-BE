package com.mapleraid.chat.application.port.in.input.query;

import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ReadDmMessagesInput {
    private final DirectMessageRoomId roomId;
    private final UserId requesterId;
    private final int limit;
    private final Instant before; // nullable - cursor

    private ReadDmMessagesInput(DirectMessageRoomId roomId, UserId requesterId, int limit, Instant before) {
        this.roomId = roomId;
        this.requesterId = requesterId;
        this.limit = limit;
        this.before = before;
    }

    public static ReadDmMessagesInput of(DirectMessageRoomId roomId, UserId requesterId, int limit, Instant before) {
        return new ReadDmMessagesInput(roomId, requesterId, limit, before);
    }
}
