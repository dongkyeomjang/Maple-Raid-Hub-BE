package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.query.ReadCharacterDetailInput;
import com.mapleraid.character.application.port.in.output.result.ReadCharacterDetailResult;
import com.mapleraid.character.application.port.in.usecase.ReadCharacterDetailUseCase;
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
public class ReadCharacterDetailService implements ReadCharacterDetailUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterSyncHelper characterSyncHelper;

    @Override
    @Transactional
    public ReadCharacterDetailResult execute(ReadCharacterDetailInput input) {
        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        Character syncedCharacter = characterSyncHelper.syncIfNeeded(character);

        return ReadCharacterDetailResult.from(syncedCharacter);
    }
}
