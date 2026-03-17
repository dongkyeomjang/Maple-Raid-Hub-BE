package com.mapleraid.security.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.external.application.port.out.NexonApiPort;
import com.mapleraid.security.application.port.in.input.command.CheckRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.CheckRecoveryChallengeResult;
import com.mapleraid.security.application.port.in.usecase.CheckRecoveryChallengeUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckRecoveryChallengeService implements CheckRecoveryChallengeUseCase {

    private final CharacterRepository characterRepository;
    private final VerificationChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final NexonApiPort nexonApiPort;
    private final JsonWebTokenUtil jsonWebTokenUtil;

    @Override
    @Transactional
    public CheckRecoveryChallengeResult execute(CheckRecoveryChallengeInput input) {
        VerificationChallenge challenge = challengeRepository.findById(input.getChallengeId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_VERIFICATION_CHALLENGE));

        Character character = characterRepository.findById(challenge.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        // 검사 가능 여부 확인
        challenge.validateForCheck();

        // 현재 심볼 조회
        NexonApiPort.SymbolEquipmentInfo currentSymbolEquipment = nexonApiPort.getSymbolEquipment(character.getOcid())
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_SYMBOL_UNAVAILABLE));

        // 현재 장착된 심볼 이름 목록
        Set<String> currentSymbols = currentSymbolEquipment.symbols().stream()
                .map(NexonApiPort.SymbolInfo::symbolName)
                .collect(Collectors.toSet());

        // 검사 수행
        VerificationChallenge.VerificationResult result = challenge.processCheck(currentSymbols);

        challengeRepository.save(challenge);

        // 성공 시 아이디와 복구 토큰 반환
        if (result.isSuccess()) {
            User owner = userRepository.findById(character.getOwnerId())
                    .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

            String recoveryToken = jsonWebTokenUtil.generateRecoveryToken(
                    owner.getId().getValue()
            );

            return CheckRecoveryChallengeResult.success(owner.getUsername(), recoveryToken);
        }

        return CheckRecoveryChallengeResult.from(result);
    }
}
