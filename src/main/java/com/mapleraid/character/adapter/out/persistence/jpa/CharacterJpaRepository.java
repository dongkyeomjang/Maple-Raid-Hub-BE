package com.mapleraid.character.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.type.EVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, String> {

    List<CharacterJpaEntity> findByIdIn(Collection<String> ids);

    List<CharacterJpaEntity> findByOwnerIdOrderByCombatPowerDesc(String ownerId);

    List<CharacterJpaEntity> findByOwnerIdAndVerificationStatusOrderByCombatPowerDesc(String ownerId, EVerificationStatus status);

    boolean existsByCharacterNameAndWorldNameAndVerificationStatus(
            String characterName, String worldName, EVerificationStatus status);

    List<CharacterJpaEntity> findByCharacterNameAndWorldName(String characterName, String worldName);

    boolean existsByOwnerIdAndCharacterNameAndWorldName(String ownerId, String characterName, String worldName);
}
