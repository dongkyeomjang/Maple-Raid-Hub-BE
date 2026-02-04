package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApplyToPostInput extends SelfValidating<ApplyToPostInput> {

    @NotNull(message = "게시글 아이디는 필수입니다.")
    private final PostId postId;

    @NotNull(message = "지원자 아이디는 필수입니다.")
    private final UserId applicantId;

    @NotNull(message = "캐릭터 아이디는 필수입니다.")
    private final CharacterId characterId;

    private final String message;

    private ApplyToPostInput(PostId postId, UserId applicantId, CharacterId characterId, String message) {
        this.postId = postId;
        this.applicantId = applicantId;
        this.characterId = characterId;
        this.message = message;
        this.validateSelf();
    }

    public static ApplyToPostInput of(PostId postId, UserId applicantId, CharacterId characterId, String message) {
        return new ApplyToPostInput(postId, applicantId, characterId, message);
    }
}
