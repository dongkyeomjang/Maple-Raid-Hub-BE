package com.mapleraid.character.adapter.out.persistence;

import com.mapleraid.character.adapter.out.persistence.jpa.CharacterJpaEntity;
import com.mapleraid.character.adapter.out.persistence.jpa.CharacterJpaRepository;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CharacterPersistenceAdapter implements CharacterRepository {

    private final CharacterJpaRepository characterJpaRepository;

    public CharacterPersistenceAdapter(CharacterJpaRepository characterJpaRepository) {
        this.characterJpaRepository = characterJpaRepository;
    }

    @Override
    public Character save(Character character) {
        CharacterJpaEntity entity = CharacterJpaEntity.fromDomain(character);
        return characterJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Character> findById(CharacterId id) {
        return characterJpaRepository.findById(id.getValue().toString())
                .map(CharacterJpaEntity::toDomain);
    }

    @Override
    public List<Character> findByIds(Set<CharacterId> ids) {
        Set<String> stringIds = ids.stream()
                .map(id -> id.getValue().toString())
                .collect(java.util.stream.Collectors.toSet());
        return characterJpaRepository.findByIdIn(stringIds).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Character> findByOwnerId(UserId ownerId) {
        return characterJpaRepository.findByOwnerIdOrderByCombatPowerDesc(ownerId.getValue().toString()).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Character> findByOwnerIdAndStatus(UserId ownerId, EVerificationStatus status) {
        return characterJpaRepository.findByOwnerIdAndVerificationStatusOrderByCombatPowerDesc(ownerId.getValue().toString(), status).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameAndWorldAndStatus(String characterName, String worldName, EVerificationStatus status) {
        return characterJpaRepository.existsByCharacterNameAndWorldNameAndVerificationStatus(characterName, worldName, status);
    }

    @Override
    public List<Character> findAllByNameAndWorld(String characterName, String worldName) {
        return characterJpaRepository.findByCharacterNameAndWorldName(characterName, worldName).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOwnerIdAndNameAndWorld(UserId ownerId, String characterName, String worldName) {
        return characterJpaRepository.existsByOwnerIdAndCharacterNameAndWorldName(
                ownerId.getValue().toString(), characterName, worldName);
    }

    @Override
    public void delete(Character character) {
        characterJpaRepository.deleteById(character.getId().getValue().toString());
    }
}
