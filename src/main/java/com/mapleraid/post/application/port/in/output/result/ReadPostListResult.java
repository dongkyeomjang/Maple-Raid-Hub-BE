package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ReadPostListResult extends SelfValidating<ReadPostListResult> {

    private final List<PostSummary> posts;
    private final long total;
    private final int page;
    private final int size;

    public ReadPostListResult(List<PostSummary> posts, long total, int page, int size) {
        this.posts = posts;
        this.total = total;
        this.page = page;
        this.size = size;
        this.validateSelf();
    }

    @Getter
    public static class PostSummary {

        private final String id;
        private final String authorId;
        private final String authorNickname;
        private final String characterId;
        private final String characterName;
        private final String characterImageUrl;
        private final String worldGroup;
        private final String worldName;
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

        public PostSummary(String id, String authorId, String authorNickname,
                           String characterId, String characterName, String characterImageUrl,
                           String worldGroup, String worldName,
                           List<String> bossIds, int requiredMembers, int currentMembers,
                           String preferredTime, String description, String status, String partyRoomId,
                           Instant createdAt, Instant updatedAt, Instant expiresAt) {
            this.id = id;
            this.authorId = authorId;
            this.authorNickname = authorNickname;
            this.characterId = characterId;
            this.characterName = characterName;
            this.characterImageUrl = characterImageUrl;
            this.worldGroup = worldGroup;
            this.worldName = worldName;
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
        }
    }
}
