package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadMyApplicationsInput {

    private final UserId userId;

    private ReadMyApplicationsInput(UserId userId) {
        this.userId = userId;
    }

    public static ReadMyApplicationsInput of(UserId userId) {
        return new ReadMyApplicationsInput(userId);
    }
}
