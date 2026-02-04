package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadMyPostsInput {

    private final UserId userId;

    private ReadMyPostsInput(UserId userId) {
        this.userId = userId;
    }

    public static ReadMyPostsInput of(UserId userId) {
        return new ReadMyPostsInput(userId);
    }
}
