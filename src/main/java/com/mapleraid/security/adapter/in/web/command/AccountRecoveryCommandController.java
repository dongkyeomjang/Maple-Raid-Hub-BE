package com.mapleraid.security.adapter.in.web.command;

import com.mapleraid.character.domain.VerificationChallengeId;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.security.adapter.in.web.dto.request.CreateRecoveryChallengeRequestDto;
import com.mapleraid.security.adapter.in.web.dto.request.ResetPasswordRequestDto;
import com.mapleraid.security.adapter.in.web.dto.response.RecoveryChallengeResponseDto;
import com.mapleraid.security.adapter.in.web.dto.response.RecoveryCheckResponseDto;
import com.mapleraid.security.application.port.in.input.command.CheckRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.input.command.CreateRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.input.command.ResetPasswordInput;
import com.mapleraid.security.application.port.in.usecase.CheckRecoveryChallengeUseCase;
import com.mapleraid.security.application.port.in.usecase.CreateRecoveryChallengeUseCase;
import com.mapleraid.security.application.port.in.usecase.ResetPasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/recovery")
@RequiredArgsConstructor
public class AccountRecoveryCommandController {

    private final CreateRecoveryChallengeUseCase createRecoveryChallengeUseCase;
    private final CheckRecoveryChallengeUseCase checkRecoveryChallengeUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    /**
     * 계정 복구용 인증 챌린지 생성
     */
    @PostMapping("/challenge")
    public ResponseDto<RecoveryChallengeResponseDto> createChallenge(
            @RequestBody CreateRecoveryChallengeRequestDto request) {

        return ResponseDto.created(
                RecoveryChallengeResponseDto.from(
                        createRecoveryChallengeUseCase.execute(
                                CreateRecoveryChallengeInput.of(
                                        request.characterName(),
                                        request.worldName()
                                )
                        )
                )
        );
    }

    /**
     * 계정 복구용 인증 검사
     */
    @PostMapping("/challenges/{challengeId}/check")
    public ResponseDto<RecoveryCheckResponseDto> checkChallenge(
            @PathVariable String challengeId) {

        return ResponseDto.ok(
                RecoveryCheckResponseDto.from(
                        checkRecoveryChallengeUseCase.execute(
                                CheckRecoveryChallengeInput.of(
                                        VerificationChallengeId.of(challengeId)
                                )
                        )
                )
        );
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseDto<Void> resetPassword(
            @RequestBody ResetPasswordRequestDto request) {

        resetPasswordUseCase.execute(
                ResetPasswordInput.of(
                        request.recoveryToken(),
                        request.newPassword()
                )
        );

        return ResponseDto.ok(null);
    }
}
