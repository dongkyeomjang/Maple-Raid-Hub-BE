package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.post.domain.PostId;
import lombok.Getter;

@Getter
public class ReadPostDetailInput {

    private final PostId postId;

    private ReadPostDetailInput(PostId postId) {
        this.postId = postId;
    }

    public static ReadPostDetailInput of(PostId postId) {
        return new ReadPostDetailInput(postId);
    }
}
