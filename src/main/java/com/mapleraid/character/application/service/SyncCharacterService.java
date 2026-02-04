package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.command.SyncCharacterInput;
import com.mapleraid.character.application.port.in.output.result.SyncCharacterResult;
import com.mapleraid.character.application.port.in.usecase.SyncCharacterUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.service.helper.CharacterSyncHelper;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncCharacterService implements SyncCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterSyncHelper characterSyncHelper;

    @Override
    @Transactional
    public SyncCharacterResult execute(SyncCharacterInput input) {
        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        Character syncedCharacter = characterSyncHelper.sync(character);

        return SyncCharacterResult.of(
                syncedCharacter.getId().getValue().toString(),
                syncedCharacter.getCharacterName(),
                syncedCharacter.getWorldName(),
                syncedCharacter.getEWorldGroup(),
                syncedCharacter.getCharacterClass(),
                syncedCharacter.getCharacterLevel(),
                syncedCharacter.getCharacterImageUrl(),
                syncedCharacter.getCombatPower(),
                syncedCharacter.getEquipmentJson(),
                syncedCharacter.getVerificationStatus(),
                syncedCharacter.getClaimedAt(),
                syncedCharacter.getVerifiedAt(),
                syncedCharacter.getLastSyncedAt()
        );
    }
}
