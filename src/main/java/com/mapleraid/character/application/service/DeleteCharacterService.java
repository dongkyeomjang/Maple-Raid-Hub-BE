package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.command.DeleteCharacterInput;
import com.mapleraid.character.application.port.in.usecase.DeleteCharacterUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCharacterService implements DeleteCharacterUseCase {

    private final CharacterRepository characterRepository;

    /**
     * 캐릭터 삭제
     */
    @Override
    @Transactional
    public void execute(DeleteCharacterInput input) {
        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        if (!character.getOwnerId().equals(input.getRequesterId())) {
            throw new CommonException(ErrorCode.CHARACTER_NOT_OWNER);
        }

        // TODO: 진행 중인 파티 확인

        characterRepository.delete(character);
    }
}
