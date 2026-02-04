package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class DeletePostInput {

    private final PostId postId;
    private final UserId requesterId;

    private DeletePostInput(PostId postId, UserId requesterId) {
        this.postId = postId;
        this.requesterId = requesterId;
    }

    public static DeletePostInput of(PostId postId, UserId requesterId) {
        return new DeletePostInput(postId, requesterId);
    }
}
