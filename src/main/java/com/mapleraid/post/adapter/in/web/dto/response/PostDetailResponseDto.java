package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadPostDetailResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponseDto(
        PostInfo post,
        List<ApplicationWithCharacterDto> applications,
        CharacterSummaryDto authorCharacter
) {
    public static PostDetailResponseDto from(ReadPostDetailResult result) {
        PostInfo post = new PostInfo(
                result.getId(), result.getAuthorId(), result.getCharacterId(),
                result.getWorldGroup(), result.getBossIds(), result.getRequiredMembers(),
                result.getCurrentMembers(), result.getPreferredTime(), result.getDescription(),
                result.getStatus(), result.getPartyRoomId(),
                result.getCreatedAt(), result.getUpdatedAt());

        List<ApplicationWithCharacterDto> apps = result.getApplications().stream()
                .map(a -> new ApplicationWithCharacterDto(
                        a.getId(), a.getApplicantId(), a.getCharacterId(),
                        a.getMessage(), a.getStatus(), a.getAppliedAt(), a.getRespondedAt(),
                        toCharacterDto(a.getCharacter())))
                .toList();

        return new PostDetailResponseDto(post, apps, toCharacterDto(result.getAuthorCharacter()));
    }

    private static CharacterSummaryDto toCharacterDto(ReadPostDetailResult.CharacterSummary c) {
        if (c == null) return null;
        return new CharacterSummaryDto(
                c.getId(), c.getCharacterName(), c.getWorldName(), c.getWorldGroup(),
                c.getCharacterClass(), c.getCharacterLevel(), c.getCharacterImageUrl(),
                c.getCombatPower(), c.getEquipmentJson(), c.getVerificationStatus(),
                c.getLastSyncedAt());
    }

    public record PostInfo(
            String id,
            String authorId,
            String characterId,
            String worldGroup,
            List<String> bossIds,
            int requiredMembers,
            int currentMembers,
            String preferredTime,
            String description,
            String status,
            String partyRoomId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ApplicationWithCharacterDto(
            String id,
            String applicantId,
            String characterId,
            String message,
            String status,
            Instant appliedAt,
            Instant respondedAt,
            CharacterSummaryDto character
    ) {
    }

    public record CharacterSummaryDto(
            String id,
            String characterName,
            String worldName,
            String worldGroup,
            String characterClass,
            int characterLevel,
            String characterImageUrl,
            long combatPower,
            String equipmentJson,
            String verificationStatus,
            LocalDateTime lastSyncedAt
    ) {
    }
}
