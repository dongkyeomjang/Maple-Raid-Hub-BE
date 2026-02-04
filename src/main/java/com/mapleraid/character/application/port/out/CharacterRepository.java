package com.mapleraid.character.application.port.out;

import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CharacterRepository {

    Character save(Character character);

    Optional<Character> findById(CharacterId id);

    List<Character> findByIds(Set<CharacterId> ids);

    List<Character> findAllByNameAndWorld(String characterName, String worldName);

    List<Character> findByOwnerId(UserId ownerId);

    List<Character> findByOwnerIdAndStatus(UserId ownerId, EVerificationStatus status);

    boolean existsByNameAndWorldAndStatus(String characterName, String worldName, EVerificationStatus status);

    boolean existsByOwnerIdAndNameAndWorld(UserId ownerId, String characterName, String worldName);

    void delete(Character character);
}
