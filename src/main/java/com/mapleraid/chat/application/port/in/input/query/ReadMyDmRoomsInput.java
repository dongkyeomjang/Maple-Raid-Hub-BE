package com.mapleraid.chat.application.port.in.input.query;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadMyDmRoomsInput {
    private final UserId userId;

    private ReadMyDmRoomsInput(UserId userId) {
        this.userId = userId;
    }

    public static ReadMyDmRoomsInput of(UserId userId) {
        return new ReadMyDmRoomsInput(userId);
    }
}
