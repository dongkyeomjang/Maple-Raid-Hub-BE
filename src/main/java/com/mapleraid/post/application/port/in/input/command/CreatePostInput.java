package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreatePostInput extends SelfValidating<CreatePostInput> {

    // 회원 작성 시
    private final UserId authorId;
    private final CharacterId characterId;

    // 비회원 작성 시
    private final boolean guest;
    private final EWorldGroup guestWorldGroup;
    private final String guestWorldName;
    private final String guestCharacterName;
    private final String contactLink;
    private final String guestPassword;

    @NotEmpty(message = "최소 1개 이상의 보스를 선택해야 합니다.")
    private final List<String> bossIds;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 6, message = "최대 6명까지 가능합니다.")
    private final int requiredMembers;

    private final String preferredTime;

    private final String description;

    private CreatePostInput(UserId authorId, CharacterId characterId,
                            boolean guest, EWorldGroup guestWorldGroup, String guestWorldName,
                            String guestCharacterName, String contactLink, String guestPassword,
                            List<String> bossIds, int requiredMembers,
                            String preferredTime, String description) {
        this.authorId = authorId;
        this.characterId = characterId;
        this.guest = guest;
        this.guestWorldGroup = guestWorldGroup;
        this.guestWorldName = guestWorldName;
        this.guestCharacterName = guestCharacterName;
        this.contactLink = contactLink;
        this.guestPassword = guestPassword;
        this.bossIds = bossIds;
        this.requiredMembers = requiredMembers;
        this.preferredTime = preferredTime;
        this.description = description;
        this.validateSelf();
    }

    public static CreatePostInput of(UserId authorId, CharacterId characterId, List<String> bossIds,
                                     int requiredMembers, String preferredTime, String description) {
        return new CreatePostInput(authorId, characterId,
                false, null, null, null, null, null,
                bossIds, requiredMembers, preferredTime, description);
    }

    public static CreatePostInput ofGuest(EWorldGroup guestWorldGroup, String guestWorldName,
                                          String guestCharacterName, String contactLink,
                                          String guestPassword,
                                          List<String> bossIds, int requiredMembers,
                                          String preferredTime, String description) {
        return new CreatePostInput(null, null,
                true, guestWorldGroup, guestWorldName, guestCharacterName, contactLink, guestPassword,
                bossIds, requiredMembers, preferredTime, description);
    }
}
