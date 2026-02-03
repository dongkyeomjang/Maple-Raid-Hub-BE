package com.mapleraid.application.service;

import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.NexonApiPort;
import com.mapleraid.application.port.out.VerificationChallengeRepository;
import com.mapleraid.domain.character.ChallengeStatus;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.VerificationChallenge;
import com.mapleraid.domain.character.VerificationChallengeId;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class VerificationService {

    private static final int DAILY_CHALLENGE_LIMIT = 5;
    private static final Duration RETRY_COOLDOWN = Duration.ofHours(1);

    // 심볼 목록과 해금 레벨
    private static final List<SymbolDefinition> ALL_SYMBOLS = List.of(
            // 아케인 심볼 (Lv 200+)
            new SymbolDefinition("아케인심볼 : 소멸의 여로", 200),
            new SymbolDefinition("아케인심볼 : 츄츄 아일랜드", 210),
            new SymbolDefinition("아케인심볼 : 레헬른", 220),
            new SymbolDefinition("아케인심볼 : 아르카나", 225),
            new SymbolDefinition("아케인심볼 : 모라스", 230),
            new SymbolDefinition("아케인심볼 : 에스페라", 235),
            // 어센틱 심볼 (그란디스)
            new SymbolDefinition("어센틱심볼 : 세르니움", 260),
            new SymbolDefinition("어센틱심볼 : 아르크스", 265),
            new SymbolDefinition("어센틱심볼 : 오디움", 270),
            new SymbolDefinition("어센틱심볼 : 도원경", 275),
            new SymbolDefinition("어센틱심볼 : 아르테리아", 280),
            new SymbolDefinition("어센틱심볼 : 카르시온", 285),
            new SymbolDefinition("어센틱심볼 : 탈라하트", 290)
    );

    private final VerificationChallengeRepository challengeRepository;
    private final CharacterRepository characterRepository;
    private final NexonApiPort nexonApiPort;

    public VerificationService(VerificationChallengeRepository challengeRepository,
                               CharacterRepository characterRepository,
                               NexonApiPort nexonApiPort) {
        this.challengeRepository = challengeRepository;
        this.characterRepository = characterRepository;
        this.nexonApiPort = nexonApiPort;
    }

    /**
     * 인증 챌린지 생성
     */
    public VerificationChallenge createChallenge(CharacterId characterId, UserId requesterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        // 본인 캐릭터인지 확인
        if (!character.getOwnerId().equals(requesterId)) {
            throw new DomainException("CHARACTER_NOT_OWNER",
                    "본인의 캐릭터만 인증할 수 있습니다.");
        }

        // 인증 가능 상태인지 확인
        if (!character.canStartVerification()) {
            throw new DomainException("CHARACTER_CANNOT_VERIFY",
                    "인증할 수 없는 상태의 캐릭터입니다.",
                    Map.of("status", character.getVerificationStatus()));
        }

        // 이미 진행 중인 챌린지 확인
        challengeRepository.findByCharacterIdAndStatus(characterId, ChallengeStatus.PENDING)
                .ifPresent(existing -> {
                    throw new DomainException("VERIFICATION_ALREADY_PENDING",
                            "이미 진행 중인 인증이 있습니다.",
                            Map.of("challengeId", existing.getId().getValue(),
                                    "expiresAt", existing.getExpiresAt()));
                });

        // 일일 챌린지 제한 확인
        Instant dayStart = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        int todayCount = challengeRepository.countByCharacterIdAndCreatedAtAfter(characterId, dayStart);
        if (todayCount >= DAILY_CHALLENGE_LIMIT) {
            throw new DomainException("VERIFICATION_DAILY_LIMIT",
                    String.format("일일 인증 시도 횟수(%d회)를 초과했습니다.", DAILY_CHALLENGE_LIMIT),
                    Map.of("resetAt", dayStart.plus(Duration.ofDays(1))));
        }

        // 실패 후 쿨다운 확인
        challengeRepository.findLastFailedAtByCharacterId(characterId)
                .ifPresent(lastFailedAt -> {
                    Instant retryAfter = lastFailedAt.plus(RETRY_COOLDOWN);
                    if (Instant.now().isBefore(retryAfter)) {
                        throw new DomainException("VERIFICATION_COOLDOWN",
                                "인증 실패 후 1시간이 지나야 재시도할 수 있습니다.",
                                Map.of("retryAfter", retryAfter));
                    }
                });

        // 캐릭터 레벨 확인
        int characterLevel = character.getCharacterLevel();
        if (characterLevel < 260) {
            throw new DomainException("CHARACTER_LEVEL_TOO_LOW",
                    "인증을 위해서는 최소 레벨 260 이상이어야 합니다.",
                    Map.of("currentLevel", characterLevel, "requiredLevel", 260));
        }

        // 현재 심볼 상태 조회
        NexonApiPort.SymbolEquipmentInfo symbolEquipment = nexonApiPort.getSymbolEquipment(character.getOcid())
                .orElseThrow(() -> new DomainException("CHARACTER_SYMBOL_UNAVAILABLE",
                        "심볼 정보를 조회할 수 없습니다."));

        // 캐릭터 레벨에 따라 선택 가능한 심볼 결정
        List<String> availableSymbols = getAvailableSymbols(characterLevel, symbolEquipment);

        if (availableSymbols.size() < 2) {
            throw new DomainException("NOT_ENOUGH_SYMBOLS",
                    "인증에 필요한 심볼이 부족합니다. 최소 2개 이상의 심볼이 필요합니다.",
                    Map.of("availableSymbols", availableSymbols.size()));
        }

        // 랜덤으로 2개 선택
        List<String> selectedSymbols = selectRandomSymbols(availableSymbols, 2);

        // Baseline 심볼 목록 저장
        String baselineSymbols = symbolEquipment.symbols().stream()
                .map(NexonApiPort.SymbolInfo::symbolName)
                .collect(Collectors.joining(","));

        // 챌린지 생성
        VerificationChallenge challenge = VerificationChallenge.create(
                characterId,
                selectedSymbols.get(0),
                selectedSymbols.get(1),
                baselineSymbols
        );

        return challengeRepository.save(challenge);
    }

    /**
     * 챌린지 상태 조회
     */
    @Transactional(readOnly = true)
    public VerificationChallenge getChallengeStatus(VerificationChallengeId challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new DomainException("VERIFICATION_CHALLENGE_NOT_FOUND",
                        "챌린지를 찾을 수 없습니다."));
    }

    /**
     * 캐릭터의 현재 진행 중인 챌린지 조회
     */
    @Transactional(readOnly = true)
    public Optional<VerificationChallenge> getPendingChallenge(CharacterId characterId) {
        return challengeRepository.findByCharacterIdAndStatus(characterId, ChallengeStatus.PENDING);
    }

    /**
     * 인증 검사 실행
     */
    public VerificationChallenge.VerificationResult checkVerification(
            VerificationChallengeId challengeId, UserId requesterId) {

        VerificationChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new DomainException("VERIFICATION_CHALLENGE_NOT_FOUND",
                        "챌린지를 찾을 수 없습니다."));

        Character character = characterRepository.findById(challenge.getCharacterId())
                .orElseThrow(() -> new DomainException("CHARACTER_NOT_FOUND",
                        "캐릭터를 찾을 수 없습니다."));

        // 본인 확인
        if (!character.getOwnerId().equals(requesterId)) {
            throw new DomainException("CHARACTER_NOT_OWNER",
                    "본인의 캐릭터만 검사할 수 있습니다.");
        }

        // 검사 가능 여부 확인 (rate limit, 만료 등)
        challenge.validateForCheck();

        // 현재 심볼 조회
        NexonApiPort.SymbolEquipmentInfo currentSymbolEquipment = nexonApiPort.getSymbolEquipment(character.getOcid())
                .orElseThrow(() -> new DomainException("CHARACTER_SYMBOL_UNAVAILABLE",
                        "심볼 정보를 조회할 수 없습니다."));

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

        return result;
    }

    /**
     * 만료된 챌린지 정리 (스케줄러에서 호출)
     */
    public int expireOldChallenges() {
        List<VerificationChallenge> expiredChallenges =
                challengeRepository.findExpiredPendingChallenges(Instant.now());

        for (VerificationChallenge challenge : expiredChallenges) {
            challenge.expire();
            challengeRepository.save(challenge);
        }

        return expiredChallenges.size();
    }

    /**
     * 캐릭터 레벨에 따라 선택 가능한 심볼 목록 반환
     * 현재 레벨보다 낮은 해금 레벨의 심볼 중 실제로 보유한 것만 반환
     */
    private List<String> getAvailableSymbols(int characterLevel, NexonApiPort.SymbolEquipmentInfo symbolEquipment) {
        // 캐릭터가 보유한 심볼 이름 목록
        Set<String> ownedSymbols = symbolEquipment.symbols().stream()
                .map(NexonApiPort.SymbolInfo::symbolName)
                .collect(Collectors.toSet());

        // 레벨에 따른 최대 선택 가능 심볼 결정
        // 280 미만: 아르테리아 이전까지 (도원경까지)
        // 285 미만: 카르시온 이전까지 (아르테리아까지)
        // 290 미만: 탈라하트 이전까지 (카르시온까지)
        // 290 이상: 모든 심볼
        int maxUnlockLevel;
        if (characterLevel < 280) {
            maxUnlockLevel = 275; // 도원경까지
        } else if (characterLevel < 285) {
            maxUnlockLevel = 280; // 아르테리아까지
        } else if (characterLevel < 290) {
            maxUnlockLevel = 285; // 카르시온까지
        } else {
            maxUnlockLevel = 290; // 탈라하트까지
        }

        return ALL_SYMBOLS.stream()
                .filter(s -> s.unlockLevel() <= maxUnlockLevel)
                .map(SymbolDefinition::name)
                .filter(ownedSymbols::contains)
                .collect(Collectors.toList());
    }

    /**
     * 목록에서 랜덤하게 N개 선택
     */
    private List<String> selectRandomSymbols(List<String> symbols, int count) {
        List<String> shuffled = new ArrayList<>(symbols);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private void revokeOtherClaims(Character verifiedCharacter) {
        // 같은 캐릭터명+월드의 다른 클레임 찾아서 모두 무효화
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

    private record SymbolDefinition(String name, int unlockLevel) {
    }
}
