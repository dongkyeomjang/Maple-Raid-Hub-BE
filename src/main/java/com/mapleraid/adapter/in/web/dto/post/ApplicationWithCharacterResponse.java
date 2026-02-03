package com.mapleraid.adapter.in.web.dto.post;

import com.mapleraid.adapter.in.web.dto.character.PublicCharacterResponse;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationStatus;

import java.time.Instant;

/**
 * 캐릭터 정보가 포함된 지원 응답
 */
public record ApplicationWithCharacterResponse(
        String id,
        String applicantId,
        String characterId,
        String message,
        ApplicationStatus status,
        Instant appliedAt,
        Instant respondedAt,
        PublicCharacterResponse character
) {
    public static ApplicationWithCharacterResponse from(Application application, Character character) {
        return new ApplicationWithCharacterResponse(
                application.getId().getValue().toString(),
                application.getApplicantId().getValue().toString(),
                application.getCharacterId().getValue().toString(),
                application.getMessage(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getRespondedAt(),
                character != null ? PublicCharacterResponse.from(character) : null
        );
    }
}
