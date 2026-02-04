package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ClosePostInput {

    private final PostId postId;
    private final UserId requesterId;

    private ClosePostInput(PostId postId, UserId requesterId) {
        this.postId = postId;
        this.requesterId = requesterId;
    }

    public static ClosePostInput of(PostId postId, UserId requesterId) {
        return new ClosePostInput(postId, requesterId);
    }
}
