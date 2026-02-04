package com.mapleraid.security.application.port.in.input.query;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadCurrentUserInput {

    private final UserId userId;

    public ReadCurrentUserInput(
            UserId userId
    ) {
        this.userId = userId;
    }

    public static ReadCurrentUserInput of(
            UserId userId
    ) {
        return new ReadCurrentUserInput(
                userId
        );
    }
}
