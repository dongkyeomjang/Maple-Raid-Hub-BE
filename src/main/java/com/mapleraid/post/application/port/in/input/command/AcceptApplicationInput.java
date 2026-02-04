package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.post.domain.ApplicationId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class AcceptApplicationInput {

    private final PostId postId;
    private final ApplicationId applicationId;
    private final UserId requesterId;

    private AcceptApplicationInput(PostId postId, ApplicationId applicationId, UserId requesterId) {
        this.postId = postId;
        this.applicationId = applicationId;
        this.requesterId = requesterId;
    }

    public static AcceptApplicationInput of(PostId postId, ApplicationId applicationId, UserId requesterId) {
        return new AcceptApplicationInput(postId, applicationId, requesterId);
    }
}
