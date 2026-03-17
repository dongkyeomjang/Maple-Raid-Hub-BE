package com.mapleraid.security.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.port.in.input.query.ReadPendingRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.ReadPendingRecoveryChallengeResult;
import com.mapleraid.security.application.port.in.usecase.ReadPendingRecoveryChallengeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadPendingRecoveryChallengeService implements ReadPendingRecoveryChallengeUseCase {

    private final CharacterRepository characterRepository;
    private final VerificationChallengeRepository challengeRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadPendingRecoveryChallengeResult execute(ReadPendingRecoveryChallengeInput input) {
        // 인증된 캐릭터 찾기
        Character character = characterRepository.findAllByNameAndWorld(input.getCharacterName(), input.getWorldName())
                .stream()
                .filter(c -> c.getVerificationStatus() == EVerificationStatus.VERIFIED_OWNER)
                .findFirst()
                .orElseThrow(() -> new CommonException(ErrorCode.RECOVERY_NO_VERIFIED_CHARACTER));

        // 진행 중인 챌린지 조회
        return challengeRepository.findByCharacterIdAndStatus(character.getId(), EChallengeStatus.PENDING)
                .map(ReadPendingRecoveryChallengeResult::from)
                .orElse(ReadPendingRecoveryChallengeResult.empty());
    }
}
