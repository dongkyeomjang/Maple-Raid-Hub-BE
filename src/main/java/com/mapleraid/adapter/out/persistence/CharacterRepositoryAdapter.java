package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.CharacterJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.CharacterJpaRepository;
import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CharacterRepositoryAdapter implements CharacterRepository {

    private final CharacterJpaRepository jpaRepository;

    public CharacterRepositoryAdapter(CharacterJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Character save(Character character) {
        CharacterJpaEntity entity = CharacterJpaEntity.fromDomain(character);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Character> findById(CharacterId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(CharacterJpaEntity::toDomain);
    }

    @Override
    public List<Character> findByIds(Set<CharacterId> ids) {
        Set<String> stringIds = ids.stream()
                .map(id -> id.getValue().toString())
                .collect(java.util.stream.Collectors.toSet());
        return jpaRepository.findByIdIn(stringIds).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Character> findByOwnerId(UserId ownerId) {
        return jpaRepository.findByOwnerIdOrderByCombatPowerDesc(ownerId.getValue().toString()).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Character> findByOwnerIdAndStatus(UserId ownerId, VerificationStatus status) {
        return jpaRepository.findByOwnerIdAndVerificationStatusOrderByCombatPowerDesc(ownerId.getValue().toString(), status).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameAndWorldAndStatus(String characterName, String worldName, VerificationStatus status) {
        return jpaRepository.existsByCharacterNameAndWorldNameAndVerificationStatus(characterName, worldName, status);
    }

    @Override
    public List<Character> findAllByNameAndWorld(String characterName, String worldName) {
        return jpaRepository.findByCharacterNameAndWorldName(characterName, worldName).stream()
                .map(CharacterJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOwnerIdAndNameAndWorld(UserId ownerId, String characterName, String worldName) {
        return jpaRepository.existsByOwnerIdAndCharacterNameAndWorldName(
                ownerId.getValue().toString(), characterName, worldName);
    }

    @Override
    public void delete(Character character) {
        jpaRepository.deleteById(character.getId().getValue().toString());
    }
}
