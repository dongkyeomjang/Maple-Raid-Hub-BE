package com.mapleraid.application.port.out;

import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CharacterRepository {

    Character save(Character character);

    Optional<Character> findById(CharacterId id);

    List<Character> findByIds(Set<CharacterId> ids);

    List<Character> findAllByNameAndWorld(String characterName, String worldName);

    List<Character> findByOwnerId(UserId ownerId);

    List<Character> findByOwnerIdAndStatus(UserId ownerId, VerificationStatus status);

    boolean existsByNameAndWorldAndStatus(String characterName, String worldName, VerificationStatus status);

    boolean existsByOwnerIdAndNameAndWorld(UserId ownerId, String characterName, String worldName);

    void delete(Character character);
}
