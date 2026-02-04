package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.command.CheckVerificationInput;
import com.mapleraid.character.application.port.in.output.result.CheckVerificationResult;
import com.mapleraid.character.application.port.in.usecase.CheckVerificationUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckVerificationService implements CheckVerificationUseCase {

    private final CharacterRepository characterRepository;
    private final VerificationChallengeRepository challengeRepository;
    private final NexonApiPort nexonApiPort;

    @Override
    @Transactional
    public CheckVerificationResult execute(CheckVerificationInput input) {
        VerificationChallenge challenge = challengeRepository.findById(input.getChallengeId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_VERIFICATION_CHALLENGE));

        Character character = characterRepository.findById(challenge.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        // 본인 확인
        if (!character.getOwnerId().equals(input.getRequesterId())) {
            throw new CommonException(ErrorCode.CHARACTER_NOT_OWNER);
        }

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

        // 결과에 따른 처리
        if (result.isSuccess()) {
            // 캐릭터 인증 완료 처리
            character.markAsVerified();
            characterRepository.save(character);

            // 같은 캐릭터의 다른 클레임 무효화
            revokeOtherClaims(character);
        }

        challengeRepository.save(challenge);

        return CheckVerificationResult.from(result);
    }


    /**
     * ============================= PRIVATE METHODS =============================
     */
    private void revokeOtherClaims(Character verifiedCharacter) {
        List<Character> sameCharacters = characterRepository.findAllByNameAndWorld(
                verifiedCharacter.getCharacterName(),
                verifiedCharacter.getWorldName());

        for (Character other : sameCharacters) {
            if (!other.getId().equals(verifiedCharacter.getId())) {
                other.revoke();
                characterRepository.save(other);
            }
        }
    }
}
