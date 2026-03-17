package com.mapleraid.security.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.application.port.out.VerificationChallengeRepository;
import com.mapleraid.character.application.service.helper.VerificationHelper;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.VerificationChallenge;
import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import com.mapleraid.security.application.port.in.input.command.CreateRecoveryChallengeInput;
import com.mapleraid.security.application.port.in.output.result.CreateRecoveryChallengeResult;
import com.mapleraid.security.application.port.in.usecase.CreateRecoveryChallengeUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
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
public class CreateRecoveryChallengeService implements CreateRecoveryChallengeUseCase {

    private static final Duration RETRY_COOLDOWN = Duration.ofHours(1);

    private final CharacterRepository characterRepository;
    private final VerificationChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final NexonApiPort nexonApiPort;
    private final VerificationHelper verificationHelper;

    @Override
    @Transactional
    public CreateRecoveryChallengeResult execute(CreateRecoveryChallengeInput input) {
        // 인증된 캐릭터 찾기
        Character character = characterRepository.findAllByNameAndWorld(input.getCharacterName(), input.getWorldName())
                .stream()
                .filter(c -> c.getVerificationStatus() == EVerificationStatus.VERIFIED_OWNER)
                .findFirst()
                .orElseThrow(() -> new CommonException(ErrorCode.RECOVERY_NO_VERIFIED_CHARACTER));

        // 해당 캐릭터의 소유자가 OAuth 유저인지 확인
        User owner = userRepository.findById(character.getOwnerId())
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        if (owner.isOAuthUser()) {
            throw new CommonException(ErrorCode.RECOVERY_OAUTH_USER);
        }

        // 이미 진행 중인 챌린지 확인
        challengeRepository.findByCharacterIdAndStatus(character.getId(), EChallengeStatus.PENDING)
                .ifPresent(existing -> {
                    throw new CommonException(ErrorCode.VERIFICATION_ALREADY_PENDING);
                });

        // 일일 챌린지 제한 확인
        LocalDateTime dayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        int todayCount = challengeRepository.countByCharacterIdAndCreatedAtAfter(character.getId(), dayStart);
        if (todayCount >= Constants.DAILY_CHALLENGE_LIMIT) {
            throw new CommonException(ErrorCode.VERIFICATION_DAILY_LIMIT);
        }

        // 실패 후 쿨다운 확인
        challengeRepository.findLastFailedAtByCharacterId(character.getId())
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
                character.getId(),
                selectedSymbols.get(0),
                selectedSymbols.get(1),
                baselineSymbols
        );

        VerificationChallenge savedChallenge = challengeRepository.save(challenge);

        return CreateRecoveryChallengeResult.from(savedChallenge);
    }
}
