package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.PostId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VerifyGuestPasswordInput extends SelfValidating<VerifyGuestPasswordInput> {

    @NotNull
    private final PostId postId;
    private final String password;

    private VerifyGuestPasswordInput(PostId postId, String password) {
        this.postId = postId;
        this.password = password;
        this.validateSelf();
    }

    public static VerifyGuestPasswordInput of(PostId postId, String password) {
        return new VerifyGuestPasswordInput(postId, password);
    }
}
