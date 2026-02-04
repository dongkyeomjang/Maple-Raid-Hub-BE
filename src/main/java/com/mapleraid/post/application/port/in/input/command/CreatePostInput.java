package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CreatePostInput extends SelfValidating<CreatePostInput> {

    @NotNull(message = "작성자 아이디는 필수입니다.")
    private final UserId authorId;

    @NotNull(message = "캐릭터 아이디는 필수입니다.")
    private final CharacterId characterId;

    @NotEmpty(message = "최소 1개 이상의 보스를 선택해야 합니다.")
    private final List<String> bossIds;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 6, message = "최대 6명까지 가능합니다.")
    private final int requiredMembers;

    private final String preferredTime;

    private final String description;

    private CreatePostInput(UserId authorId, CharacterId characterId, List<String> bossIds,
                            int requiredMembers, String preferredTime, String description) {
        this.authorId = authorId;
        this.characterId = characterId;
        this.bossIds = bossIds;
        this.requiredMembers = requiredMembers;
        this.preferredTime = preferredTime;
        this.description = description;
        this.validateSelf();
    }

    public static CreatePostInput of(UserId authorId, CharacterId characterId, List<String> bossIds,
                                     int requiredMembers, String preferredTime, String description) {
        return new CreatePostInput(authorId, characterId, bossIds, requiredMembers, preferredTime, description);
    }
}
