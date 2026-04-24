package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ClosePostResult;
import com.mapleraid.post.application.port.in.output.result.CreatePostResult;
import com.mapleraid.post.application.port.in.output.result.ReadMyPostsResult;
import com.mapleraid.post.application.port.in.output.result.ReadPostListResult;
import com.mapleraid.post.application.port.in.output.result.UpdatePostResult;

import java.time.Instant;
import java.util.List;

public record PostResponseDto(
        String id,
        String authorId,
        String authorNickname,
        String characterId,
        String characterName,
        String characterImageUrl,
        String worldGroup,
        String worldName,
        boolean guest,
        String contactLink,
        List<String> bossIds,
        int requiredMembers,
        int currentMembers,
        String preferredTime,
        String description,
        String status,
        String partyRoomId,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt,
        int pendingApplicationCount
) {
    public static PostResponseDto from(CreatePostResult result) {
        return new PostResponseDto(result.getId(), result.getAuthorId(), result.getAuthorNickname(),
                result.getCharacterId(), result.getCharacterName(), result.getCharacterImageUrl(),
                result.getWorldGroup(), result.getWorldName(),
                result.isGuest(), result.getContactLink(),
                result.getBossIds(), result.getRequiredMembers(),
                result.getCurrentMembers(), result.getPreferredTime(), result.getDescription(),
                result.getStatus(), result.getPartyRoomId(),
                result.getCreatedAt(), result.getUpdatedAt(), result.getExpiresAt(), 0);
    }

    public static PostResponseDto from(UpdatePostResult result) {
        return new PostResponseDto(result.getId(), result.getAuthorId(), result.getAuthorNickname(),
                result.getCharacterId(), result.getCharacterName(), result.getCharacterImageUrl(),
                result.getWorldGroup(), null,
                false, null,
                result.getBossIds(), result.getRequiredMembers(),
                result.getCurrentMembers(), result.getPreferredTime(), result.getDescription(),
                result.getStatus(), result.getPartyRoomId(),
                result.getCreatedAt(), result.getUpdatedAt(), result.getExpiresAt(), 0);
    }

    public static PostResponseDto from(ClosePostResult result) {
        return new PostResponseDto(result.getId(), result.getAuthorId(), result.getAuthorNickname(),
                result.getCharacterId(), result.getCharacterName(), result.getCharacterImageUrl(),
                result.getWorldGroup(), null,
                false, null,
                result.getBossIds(), result.getRequiredMembers(),
                result.getCurrentMembers(), result.getPreferredTime(), result.getDescription(),
                result.getStatus(), result.getPartyRoomId(),
                result.getCreatedAt(), result.getUpdatedAt(), result.getExpiresAt(), 0);
    }

    public static PostResponseDto from(ReadPostListResult.PostSummary summary) {
        return new PostResponseDto(summary.getId(), summary.getAuthorId(), summary.getAuthorNickname(),
                summary.getCharacterId(), summary.getCharacterName(), summary.getCharacterImageUrl(),
                summary.getWorldGroup(), summary.getWorldName(),
                summary.isGuest(), summary.getContactLink(),
                summary.getBossIds(), summary.getRequiredMembers(),
                summary.getCurrentMembers(), summary.getPreferredTime(), summary.getDescription(),
                summary.getStatus(), summary.getPartyRoomId(),
                summary.getCreatedAt(), summary.getUpdatedAt(), summary.getExpiresAt(), 0);
    }

    public static PostResponseDto from(ReadMyPostsResult.PostSummary summary) {
        return new PostResponseDto(summary.getId(), summary.getAuthorId(), summary.getAuthorNickname(),
                summary.getCharacterId(), summary.getCharacterName(), summary.getCharacterImageUrl(),
                summary.getWorldGroup(), summary.getWorldName(),
                summary.isGuest(), summary.getContactLink(),
                summary.getBossIds(), summary.getRequiredMembers(),
                summary.getCurrentMembers(), summary.getPreferredTime(), summary.getDescription(),
                summary.getStatus(), summary.getPartyRoomId(),
                summary.getCreatedAt(), summary.getUpdatedAt(), summary.getExpiresAt(),
                summary.getPendingApplicationCount());
    }
}
