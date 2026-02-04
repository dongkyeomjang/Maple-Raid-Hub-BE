package com.mapleraid.character.adapter.in.web.command;

import com.mapleraid.character.adapter.in.web.dto.request.ClaimCharacterRequestDto;
import com.mapleraid.character.adapter.in.web.dto.response.ClaimCharacterResponseDto;
import com.mapleraid.character.adapter.in.web.dto.response.SyncCharacterResponseDto;
import com.mapleraid.character.application.port.in.input.command.ClaimCharacterInput;
import com.mapleraid.character.application.port.in.input.command.DeleteCharacterInput;
import com.mapleraid.character.application.port.in.input.command.SyncCharacterInput;
import com.mapleraid.character.application.port.in.usecase.ClaimCharacterUseCase;
import com.mapleraid.character.application.port.in.usecase.DeleteCharacterUseCase;
import com.mapleraid.character.application.port.in.usecase.SyncCharacterUseCase;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterCommandController {

    private final ClaimCharacterUseCase claimCharacterUseCase;
    private final DeleteCharacterUseCase deleteCharacterUseCase;
    private final SyncCharacterUseCase syncCharacterUseCase;

    /**
     * 캐릭터 등록하기
     */
    @PostMapping
    public ResponseDto<ClaimCharacterResponseDto> claimCharacter(
            @CurrentUser UserId userId,
            @RequestBody ClaimCharacterRequestDto request) {

        return ResponseDto.created(
                ClaimCharacterResponseDto.from(
                        claimCharacterUseCase.execute(
                                ClaimCharacterInput.of(
                                        userId,
                                        request.characterName(),
                                        request.worldName()
                                )
                        )
                )
        );
    }

    /**
     * 캐릭터 삭제하기
     */
    @DeleteMapping("/{characterId}")
    public ResponseDto<Void> deleteCharacter(
            @CurrentUser UserId userId,
            @PathVariable String characterId) {

        deleteCharacterUseCase.execute(
                DeleteCharacterInput.of(
                        userId,
                        CharacterId.of(characterId)
                )
        );
        return ResponseDto.ok(null);
    }

    /**
     * 캐릭터 정보 동기화하기
     */
    @PostMapping("/{characterId}/sync")
    public ResponseDto<SyncCharacterResponseDto> syncCharacter(
            @PathVariable String characterId) {

        return ResponseDto.ok(
                SyncCharacterResponseDto.from(
                        syncCharacterUseCase.execute(
                                SyncCharacterInput.of(
                                        CharacterId.of(characterId)
                                )
                        )
                )
        );
    }
}
