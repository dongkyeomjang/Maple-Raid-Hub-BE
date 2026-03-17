package com.mapleraid.security.adapter.in.web.query;

import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.security.adapter.in.web.dto.response.RecoveryChallengeResponseDto;
import com.mapleraid.security.application.port.in.input.query.ReadPendingRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.usecase.ReadPendingRecoveryChallengeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/recovery")
@RequiredArgsConstructor
public class AccountRecoveryQueryController {

    private final ReadPendingRecoveryChallengeUseCase readPendingRecoveryChallengeUseCase;

    /**
     * 진행 중인 계정 복구 챌린지 조회
     */
    @GetMapping("/challenges/pending")
    public ResponseDto<RecoveryChallengeResponseDto> getPendingChallenge(
            @RequestParam String characterName,
            @RequestParam String worldName) {

        return ResponseDto.ok(
                RecoveryChallengeResponseDto.from(
                        readPendingRecoveryChallengeUseCase.execute(
                                ReadPendingRecoveryChallengeInput.of(characterName, worldName)
                        )
                )
        );
    }
}
