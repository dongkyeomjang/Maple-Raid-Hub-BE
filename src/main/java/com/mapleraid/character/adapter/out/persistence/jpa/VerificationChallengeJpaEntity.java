package com.mapleraid.character.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.character.domain.type.EChallengeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "verification_challenges")
public class VerificationChallengeJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "character_id", nullable = false, length = 36)
    private String characterId;

    @Column(name = "required_symbol_1", nullable = false, length = 100)
    private String requiredSymbol1;

    @Column(name = "required_symbol_2", nullable = false, length = 100)
    private String requiredSymbol2;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EChallengeStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "check_count")
    private int checkCount;

    @Column(name = "baseline_symbols", length = 1000)
    private String baselineSymbols;

    public static VerificationChallengeJpaEntity fromDomain(VerificationChallenge challenge) {
        VerificationChallengeJpaEntity entity = new VerificationChallengeJpaEntity();
        entity.id = challenge.getId().getValue().toString();
        entity.characterId = challenge.getCharacterId().getValue().toString();
        entity.requiredSymbol1 = challenge.getRequiredSymbol1();
        entity.requiredSymbol2 = challenge.getRequiredSymbol2();
        entity.status = challenge.getStatus();
        entity.createdAt = challenge.getCreatedAt();
        entity.expiresAt = challenge.getExpiresAt();
        entity.lastCheckedAt = challenge.getLastCheckedAt();
        entity.checkCount = challenge.getCheckCount();
        entity.baselineSymbols = challenge.getBaselineSymbols();
        return entity;
    }

    public VerificationChallenge toDomain() {
        return VerificationChallenge.reconstitute(
                VerificationChallengeId.of(id),
                CharacterId.of(characterId),
                requiredSymbol1,
                requiredSymbol2,
                status,
                createdAt,
                expiresAt,
                lastCheckedAt,
                checkCount,
                baselineSymbols
        );
    }
}
