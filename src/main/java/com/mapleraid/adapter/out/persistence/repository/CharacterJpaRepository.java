package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.CharacterJpaEntity;
import com.mapleraid.domain.character.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, String> {

    List<CharacterJpaEntity> findByIdIn(Collection<String> ids);

    List<CharacterJpaEntity> findByOwnerIdOrderByCombatPowerDesc(String ownerId);

    List<CharacterJpaEntity> findByOwnerIdAndVerificationStatusOrderByCombatPowerDesc(String ownerId, VerificationStatus status);

    boolean existsByCharacterNameAndWorldNameAndVerificationStatus(
            String characterName, String worldName, VerificationStatus status);

    List<CharacterJpaEntity> findByCharacterNameAndWorldName(String characterName, String worldName);

    boolean existsByOwnerIdAndCharacterNameAndWorldName(String ownerId, String characterName, String worldName);
}
