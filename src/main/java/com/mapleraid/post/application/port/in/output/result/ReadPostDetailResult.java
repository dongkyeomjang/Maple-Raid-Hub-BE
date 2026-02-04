package com.mapleraid.post.application.port.in.output.result;

import com.mapleraid.core.dto.SelfValidating;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReadPostDetailResult extends SelfValidating<ReadPostDetailResult> {

    private final String id;
    private final String authorId;
    private final String characterId;
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
    private final Instant closedAt;
    private final List<ApplicationSummary> applications;
    private final CharacterSummary authorCharacter;

    public ReadPostDetailResult(String id, String authorId, String characterId, String worldGroup,
                                List<String> bossIds, int requiredMembers, int currentMembers,
                                String preferredTime, String description, String status,
                                String partyRoomId, Instant createdAt, Instant updatedAt,
                                Instant expiresAt, Instant closedAt,
                                List<ApplicationSummary> applications,
                                CharacterSummary authorCharacter) {
        this.id = id;
        this.authorId = authorId;
        this.characterId = characterId;
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
        this.closedAt = closedAt;
        this.applications = applications;
        this.authorCharacter = authorCharacter;
        this.validateSelf();
    }

    @Getter
    public static class ApplicationSummary {

        private final String id;
        private final String applicantId;
        private final String characterId;
        private final String message;
        private final String status;
        private final Instant appliedAt;
        private final Instant respondedAt;
        private final CharacterSummary character;

        public ApplicationSummary(String id, String applicantId, String characterId,
                                  String message, String status,
                                  Instant appliedAt, Instant respondedAt,
                                  CharacterSummary character) {
            this.id = id;
            this.applicantId = applicantId;
            this.characterId = characterId;
            this.message = message;
            this.status = status;
            this.appliedAt = appliedAt;
            this.respondedAt = respondedAt;
            this.character = character;
        }
    }

    @Getter
    public static class CharacterSummary {

        private final String id;
        private final String characterName;
        private final String worldName;
        private final String worldGroup;
        private final String characterClass;
        private final int characterLevel;
        private final String characterImageUrl;
        private final long combatPower;
        private final String equipmentJson;
        private final String verificationStatus;
        private final LocalDateTime lastSyncedAt;

        public CharacterSummary(String id, String characterName, String worldName,
                                String worldGroup, String characterClass, int characterLevel,
                                String characterImageUrl, long combatPower, String equipmentJson,
                                String verificationStatus, LocalDateTime lastSyncedAt) {
            this.id = id;
            this.characterName = characterName;
            this.worldName = worldName;
            this.worldGroup = worldGroup;
            this.characterClass = characterClass;
            this.characterLevel = characterLevel;
            this.characterImageUrl = characterImageUrl;
            this.combatPower = combatPower;
            this.equipmentJson = equipmentJson;
            this.verificationStatus = verificationStatus;
            this.lastSyncedAt = lastSyncedAt;
        }
    }
}
