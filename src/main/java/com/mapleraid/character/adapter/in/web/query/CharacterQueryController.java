package com.mapleraid.character.adapter.in.web.query;

import com.mapleraid.character.adapter.in.web.dto.response.ReadCharacterDetailResponseDto;
import com.mapleraid.character.adapter.in.web.dto.response.ReadMyCharactersResponseDto;
import com.mapleraid.character.application.port.in.input.query.ReadCharacterDetailInput;
import com.mapleraid.character.application.port.in.input.query.ReadMyCharactersInput;
import com.mapleraid.character.application.port.in.usecase.ReadCharacterDetailUseCase;
import com.mapleraid.character.application.port.in.usecase.ReadMyCharactersUseCase;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterQueryController {

    private final ReadMyCharactersUseCase readMyCharactersUseCase;
    private final ReadCharacterDetailUseCase readCharacterDetailUseCase;

    /**
     * 내 캐릭터들 조회하기
     */
    @GetMapping
    public ResponseDto<ReadMyCharactersResponseDto> getMyCharacters(
            @CurrentUser UserId userId) {
        return ResponseDto.ok(
                ReadMyCharactersResponseDto.from(
                        readMyCharactersUseCase.execute(
                                ReadMyCharactersInput.of(userId)
                        )
                )
        );
    }

    /**
     * 캐릭터 상세 조회하기
     */
    @GetMapping("/{characterId}")
    public ResponseDto<ReadCharacterDetailResponseDto> getCharacter(
            @PathVariable String characterId) {
        return ResponseDto.ok(
                ReadCharacterDetailResponseDto.from(
                        readCharacterDetailUseCase.execute(
                                ReadCharacterDetailInput.of(
                                        CharacterId.of(characterId)
                                )
                        )
                )
        );
    }
}
