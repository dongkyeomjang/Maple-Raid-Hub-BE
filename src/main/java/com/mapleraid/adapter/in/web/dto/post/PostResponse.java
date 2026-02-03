package com.mapleraid.adapter.in.web.dto.post;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.post.Post;
import com.mapleraid.domain.post.PostStatus;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        String id,
        String authorId,
        String authorNickname,
        String characterId,
        String characterName,
        String characterImageUrl,
        WorldGroup worldGroup,
        List<String> bossIds,
        int requiredMembers,
        int currentMembers,
        String preferredTime,
        String description,
        PostStatus status,
        String partyRoomId,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return from(post, null, null);
    }

    public static PostResponse from(Post post, String authorNickname, Character character) {
        return new PostResponse(
                post.getId().getValue().toString(),
                post.getAuthorId().getValue().toString(),
                authorNickname,
                post.getCharacterId().getValue().toString(),
                character != null ? character.getCharacterName() : null,
                character != null ? character.getCharacterImageUrl() : null,
                post.getWorldGroup(),
                post.getBossIds(),
                post.getRequiredMembers(),
                post.getCurrentMembers(),
                post.getPreferredTime(),
                post.getDescription(),
                post.getStatus(),
                post.getPartyRoomId() != null ? post.getPartyRoomId().getValue().toString() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
