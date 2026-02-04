package com.mapleraid.chat.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateDmRoomInput extends SelfValidating<CreateDmRoomInput> {

    private final PostId postId;

    @NotNull(message = "요청자 아이디는 필수입니다.")
    private final UserId requesterId;

    @NotNull(message = "대상 사용자 아이디는 필수입니다.")
    private final UserId targetUserId;

    private final CharacterId requesterCharacterId;

    private final CharacterId targetCharacterId;

    private CreateDmRoomInput(PostId postId, UserId requesterId, UserId targetUserId,
                              CharacterId requesterCharacterId, CharacterId targetCharacterId) {
        this.postId = postId;
        this.requesterId = requesterId;
        this.targetUserId = targetUserId;
        this.requesterCharacterId = requesterCharacterId;
        this.targetCharacterId = targetCharacterId;
        this.validateSelf();
    }

    public static CreateDmRoomInput of(PostId postId, UserId requesterId, UserId targetUserId,
                                       CharacterId requesterCharacterId, CharacterId targetCharacterId) {
        return new CreateDmRoomInput(postId, requesterId, targetUserId, requesterCharacterId, targetCharacterId);
    }
}
