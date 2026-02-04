package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.post.domain.Post;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ClosePostResult extends SelfValidating<ClosePostResult> {

    private final String id;
    private final String authorId;
    private final String authorNickname;
    private final String characterId;
    private final String characterName;
    private final String characterImageUrl;
    private final String worldGroup;
    private final List<String> bossIds;
    private final int requiredMembers;
    private final int currentMembers;
    private final String preferredTime;
    private final String description;
    private final String status;
    private final String partyRoomId;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant expiresAt;

    public ClosePostResult(String id, String authorId, String authorNickname,
                           String characterId, String characterName, String characterImageUrl,
                           String worldGroup,
                           List<String> bossIds, int requiredMembers, int currentMembers,
                           String preferredTime, String description,
                           String status, String partyRoomId,
                           Instant createdAt, Instant updatedAt, Instant expiresAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.characterId = characterId;
        this.characterName = characterName;
        this.characterImageUrl = characterImageUrl;
        this.worldGroup = worldGroup;
        this.bossIds = bossIds;
        this.requiredMembers = requiredMembers;
        this.currentMembers = currentMembers;
        this.preferredTime = preferredTime;
        this.description = description;
        this.status = status;
        this.partyRoomId = partyRoomId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.validateSelf();
    }

    public static ClosePostResult from(Post post, String authorNickname, String characterName, String characterImageUrl) {
        return new ClosePostResult(
                post.getId().getValue().toString(),
                post.getAuthorId().getValue().toString(),
                authorNickname,
                post.getCharacterId().getValue().toString(),
                characterName,
                characterImageUrl,
                post.getWorldGroup().name(),
                post.getBossIds(),
                post.getRequiredMembers(),
                post.getCurrentMembers(),
                post.getPreferredTime(),
                post.getDescription(),
                post.getStatus().name(),
                post.getPartyRoomId() != null ? post.getPartyRoomId().getValue().toString() : null,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getExpiresAt());
    }
}
