package com.mapleraid.character.application.service;

import com.mapleraid.character.application.port.in.input.command.CreateVerificationChallengeInput;
import com.mapleraid.character.application.port.in.output.result.CreateVerificationChallengeResult;
import com.mapleraid.character.application.port.in.usecase.CreateVerificationChallengeUseCase;
import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.application.service.helper.VerificationHelper;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateVerificationChallengeService implements CreateVerificationChallengeUseCase {

    private static final Duration RETRY_COOLDOWN = Duration.ofHours(1);

    private final CharacterRepository characterRepository;
    private final VerificationChallengeRepository challengeRepository;
    private final NexonApiPort nexonApiPort;
    private final VerificationHelper verificationHelper;

    @Override
    @Transactional
    public CreateVerificationChallengeResult execute(CreateVerificationChallengeInput input) {
        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        // 본인 캐릭터인지 확인
        if (!character.getOwnerId().equals(input.getRequesterId())) {
            throw new CommonException(ErrorCode.CHARACTER_NOT_OWNER);
        }

        // 인증 가능 상태인지 확인
        if (!character.canStartVerification()) {
            throw new CommonException(ErrorCode.CHARACTER_CANNOT_VERIFY);
        }

        // 이미 진행 중인 챌린지 확인
        challengeRepository.findByCharacterIdAndStatus(input.getCharacterId(), EChallengeStatus.PENDING)
                .ifPresent(existing -> {
                    throw new CommonException(ErrorCode.VERIFICATION_ALREADY_PENDING);
                });

        // 일일 챌린지 제한 확인
        LocalDateTime dayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        int todayCount = challengeRepository.countByCharacterIdAndCreatedAtAfter(input.getCharacterId(), dayStart);
        if (todayCount >= Constants.DAILY_CHALLENGE_LIMIT) {
            throw new CommonException(ErrorCode.VERIFICATION_DAILY_LIMIT);
        }

        // 실패 후 쿨다운 확인
        challengeRepository.findLastFailedAtByCharacterId(input.getCharacterId())
                .ifPresent(lastFailedAt -> {
                    LocalDateTime retryAfter = lastFailedAt.plus(RETRY_COOLDOWN);
                    if (LocalDateTime.now().isBefore(retryAfter)) {
                        throw new CommonException(ErrorCode.VERIFICATION_COOLDOWN);
                    }
                });

        // 캐릭터 레벨 확인
        int characterLevel = character.getCharacterLevel();
        if (characterLevel < 260) {
            throw new CommonException(ErrorCode.CHARACTER_LEVEL_TOO_LOW);
        }

        // 현재 심볼 상태 조회
        NexonApiPort.SymbolEquipmentInfo symbolEquipment = nexonApiPort.getSymbolEquipment(character.getOcid())
                .orElseThrow(() -> new CommonException(ErrorCode.CHARACTER_SYMBOL_UNAVAILABLE));

        // 캐릭터 레벨에 따라 선택 가능한 심볼 결정
        List<String> availableSymbols = verificationHelper.getAvailableSymbols(characterLevel, symbolEquipment);

        if (availableSymbols.size() < 2) {
            throw new CommonException(ErrorCode.NOT_ENOUGH_SYMBOLS);
        }

        // 랜덤으로 2개 선택
        List<String> selectedSymbols = verificationHelper.selectRandomSymbols(availableSymbols, 2);

        // Baseline 심볼 목록 저장
        String baselineSymbols = symbolEquipment.symbols().stream()
                .map(NexonApiPort.SymbolInfo::symbolName)
                .collect(Collectors.joining(","));

        // 챌린지 생성
        VerificationChallenge challenge = VerificationChallenge.create(
                input.getCharacterId(),
                selectedSymbols.get(0),
                selectedSymbols.get(1),
                baselineSymbols
        );

        VerificationChallenge savedChallenge = challengeRepository.save(challenge);

        return CreateVerificationChallengeResult.from(savedChallenge);
    }
}
