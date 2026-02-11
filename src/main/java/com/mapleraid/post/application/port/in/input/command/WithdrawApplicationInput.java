package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.post.domain.ApplicationId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import lombok.Getter;

@Getter
public class WithdrawApplicationInput {

    private final PostId postId;
    private final ApplicationId applicationId;
    private final UserId requesterId;

    private WithdrawApplicationInput(PostId postId, ApplicationId applicationId, UserId requesterId) {
        this.postId = postId;
        this.applicationId = applicationId;
        this.requesterId = requesterId;
    }

    public static WithdrawApplicationInput of(PostId postId, ApplicationId applicationId, UserId requesterId) {
        return new WithdrawApplicationInput(postId, applicationId, requesterId);
    }
}
