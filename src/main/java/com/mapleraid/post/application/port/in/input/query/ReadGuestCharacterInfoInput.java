package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.PostId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReadGuestCharacterInfoInput extends SelfValidating<ReadGuestCharacterInfoInput> {

    @NotNull
    private final PostId postId;

    private ReadGuestCharacterInfoInput(PostId postId) {
        this.postId = postId;
        this.validateSelf();
    }

    public static ReadGuestCharacterInfoInput of(PostId postId) {
        return new ReadGuestCharacterInfoInput(postId);
    }
}
