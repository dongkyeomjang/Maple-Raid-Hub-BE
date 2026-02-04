package com.mapleraid.chat.application.port.in.input.query;

import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadDmRoomInput {
    private final DirectMessageRoomId roomId;
    private final UserId requesterId;

    private ReadDmRoomInput(DirectMessageRoomId roomId, UserId requesterId) {
        this.roomId = roomId;
        this.requesterId = requesterId;
    }

    public static ReadDmRoomInput of(DirectMessageRoomId roomId, UserId requesterId) {
        return new ReadDmRoomInput(roomId, requesterId);
    }
}
