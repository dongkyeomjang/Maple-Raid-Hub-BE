package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class DeletePostInput {

    private final PostId postId;
    private final UserId requesterId;
    private final String guestPassword;

    private DeletePostInput(PostId postId, UserId requesterId, String guestPassword) {
        this.postId = postId;
        this.requesterId = requesterId;
        this.guestPassword = guestPassword;
    }

    public static DeletePostInput of(PostId postId, UserId requesterId) {
        return new DeletePostInput(postId, requesterId, null);
    }

    public static DeletePostInput ofGuest(PostId postId, String guestPassword) {
        return new DeletePostInput(postId, null, guestPassword);
    }
}
