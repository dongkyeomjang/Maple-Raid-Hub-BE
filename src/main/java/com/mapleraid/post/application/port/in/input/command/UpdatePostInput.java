package com.mapleraid.post.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdatePostInput extends SelfValidating<UpdatePostInput> {

    @NotNull(message = "게시글 아이디는 필수입니다.")
    private final PostId postId;

    @NotNull(message = "요청자 아이디는 필수입니다.")
    private final UserId requesterId;

    private final List<String> bossIds;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 6, message = "최대 6명까지 가능합니다.")
    private final Integer requiredMembers;

    private final String preferredTime;

    private final boolean clearPreferredTime;

    private final String description;

    private final boolean clearDescription;

    private UpdatePostInput(PostId postId, UserId requesterId, List<String> bossIds,
                            Integer requiredMembers, String preferredTime, boolean clearPreferredTime,
                            String description, boolean clearDescription) {
        this.postId = postId;
        this.requesterId = requesterId;
        this.bossIds = bossIds;
        this.requiredMembers = requiredMembers;
        this.preferredTime = preferredTime;
        this.clearPreferredTime = clearPreferredTime;
        this.description = description;
        this.clearDescription = clearDescription;
        this.validateSelf();
    }

    public static UpdatePostInput of(PostId postId, UserId requesterId, List<String> bossIds,
                                     Integer requiredMembers, String preferredTime, boolean clearPreferredTime,
                                     String description, boolean clearDescription) {
        return new UpdatePostInput(postId, requesterId, bossIds, requiredMembers,
                preferredTime, clearPreferredTime, description, clearDescription);
    }
}
