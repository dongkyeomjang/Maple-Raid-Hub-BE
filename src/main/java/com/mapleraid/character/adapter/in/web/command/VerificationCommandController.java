package com.mapleraid.character.adapter.in.web.command;

import com.mapleraid.character.adapter.in.web.dto.response.CheckVerificationResponseDto;
import com.mapleraid.character.adapter.in.web.dto.response.CreateVerificationChallengeResponseDto;
import com.mapleraid.character.application.port.in.input.command.CheckVerificationInput;
import com.mapleraid.character.application.port.in.input.command.CreateVerificationChallengeInput;
import com.mapleraid.character.application.port.in.usecase.CheckVerificationUseCase;
import com.mapleraid.character.application.port.in.usecase.CreateVerificationChallengeUseCase;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationCommandController {

    private final CreateVerificationChallengeUseCase createVerificationChallengeUseCase;
    private final CheckVerificationUseCase checkVerificationUseCase;

    /**
     * 인증 챌린지 생성
     */
    @PostMapping("/characters/{characterId}/challenges")
    public ResponseDto<CreateVerificationChallengeResponseDto> createChallenge(
            @CurrentUser UserId userId,
            @PathVariable String characterId) {

        return ResponseDto.created(
                CreateVerificationChallengeResponseDto.from(
                        createVerificationChallengeUseCase.execute(
                                CreateVerificationChallengeInput.of(
                                        CharacterId.of(characterId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 인증 검사 실행
     */
    @PostMapping("/challenges/{challengeId}/check")
    public ResponseDto<CheckVerificationResponseDto> checkVerification(
            @CurrentUser UserId userId,
            @PathVariable String challengeId) {

        return ResponseDto.ok(
                CheckVerificationResponseDto.from(
                        checkVerificationUseCase.execute(
                                CheckVerificationInput.of(
                                        VerificationChallengeId.of(challengeId),
                                        userId
                                )
                        )
                )
        );
    }
}
