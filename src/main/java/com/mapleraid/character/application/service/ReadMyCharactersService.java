package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.query.ReadMyCharactersInput;
import com.mapleraid.character.application.port.in.output.result.ReadMyCharactersResult;
import com.mapleraid.character.application.port.in.usecase.ReadMyCharactersUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.service.helper.CharacterSyncHelper;
import com.mapleraid.character.domain.Character;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadMyCharactersService implements ReadMyCharactersUseCase {

    private final CharacterRepository characterRepository;
    private final CharacterSyncHelper characterSyncHelper;

    @Override
    @Transactional
    public ReadMyCharactersResult execute(ReadMyCharactersInput input) {
        List<Character> characters = characterRepository.findByOwnerId(input.getUserId());

        List<Character> syncedCharacters = characters.stream()
                .map(characterSyncHelper::syncIfNeeded)
                .toList();

        return ReadMyCharactersResult.from(syncedCharacters);
    }
}
