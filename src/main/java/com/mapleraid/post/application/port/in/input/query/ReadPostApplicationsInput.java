package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class ReadPostApplicationsInput {

    private final PostId postId;
    private final UserId requesterId;

    private ReadPostApplicationsInput(PostId postId, UserId requesterId) {
        this.postId = postId;
        this.requesterId = requesterId;
    }

    public static ReadPostApplicationsInput of(PostId postId, UserId requesterId) {
        return new ReadPostApplicationsInput(postId, requesterId);
    }
}
