package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.query.ReadPendingChallengeInput;
import com.mapleraid.character.application.port.in.output.result.ReadPendingChallengeResult;
import com.mapleraid.character.application.port.in.usecase.ReadPendingChallengeUseCase;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.domain.type.EChallengeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadPendingChallengeService implements ReadPendingChallengeUseCase {

    private final VerificationChallengeRepository challengeRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadPendingChallengeResult execute(ReadPendingChallengeInput input) {
        return challengeRepository.findByCharacterIdAndStatus(input.getCharacterId(), EChallengeStatus.PENDING)
                .map(ReadPendingChallengeResult::from)
                .orElse(ReadPendingChallengeResult.empty());
    }
}
