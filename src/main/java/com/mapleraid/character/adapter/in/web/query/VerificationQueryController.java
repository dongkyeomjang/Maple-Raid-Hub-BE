package com.mapleraid.character.adapter.in.web.query;

import com.mapleraid.character.adapter.in.web.dto.response.ReadPendingChallengeResponseDto;
import com.mapleraid.character.application.port.in.input.query.ReadPendingChallengeInput;
import com.mapleraid.character.application.port.in.usecase.ReadPendingChallengeUseCase;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationQueryController {

    private final ReadPendingChallengeUseCase readPendingChallengeUseCase;

    /**
     * 진행 중인 챌린지 조회
     */
    @GetMapping("/characters/{characterId}/challenges/pending")
    public ResponseDto<ReadPendingChallengeResponseDto> getPendingChallenge(
            @PathVariable String characterId) {

        return ResponseDto.ok(
                ReadPendingChallengeResponseDto.from(
                        readPendingChallengeUseCase.execute(
                                ReadPendingChallengeInput.of(
                                        CharacterId.of(characterId)
                                )
                        )
                )
        );
    }
}
