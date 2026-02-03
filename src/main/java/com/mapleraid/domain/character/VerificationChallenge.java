package com.mapleraid.domain.character;

import com.mapleraid.domain.common.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 캐릭터 소유권 인증 챌린지
 * 특정 심볼 두 개를 해제하도록 요청
 */
public class VerificationChallenge {

    private static final Duration CHALLENGE_DURATION = Duration.ofMinutes(30);
    private static final Duration MIN_CHECK_INTERVAL = Duration.ofSeconds(30);
    private static final Duration CHECK_INTERVAL_TOLERANCE = Duration.ofSeconds(3); // 네트워크 지연 허용
    private static final int MAX_CHECK_COUNT = 10;

    private final VerificationChallengeId id;
    private final CharacterId characterId;
    private final String requiredSymbol1;  // 해제해야 할 심볼 1
    private final String requiredSymbol2;  // 해제해야 할 심볼 2
    private final Instant createdAt;
    private final Instant expiresAt;
    private ChallengeStatus status;
    private Instant lastCheckedAt;
    private int checkCount;
    private String baselineSymbols; // 챌린지 시작 시 장착된 심볼 목록 (쉼표 구분)

    // 새 챌린지 생성용 생성자
    private VerificationChallenge(VerificationChallengeId id, CharacterId characterId,
                                  String requiredSymbol1, String requiredSymbol2,
                                  String baselineSymbols) {
        this.id = Objects.requireNonNull(id);
        this.characterId = Objects.requireNonNull(characterId);
        this.requiredSymbol1 = Objects.requireNonNull(requiredSymbol1);
        this.requiredSymbol2 = Objects.requireNonNull(requiredSymbol2);
        this.baselineSymbols = baselineSymbols;
        this.status = ChallengeStatus.PENDING;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plus(CHALLENGE_DURATION);
        this.checkCount = 0;
    }

    // DB 복원용 생성자
    private VerificationChallenge(VerificationChallengeId id, CharacterId characterId,
                                  String requiredSymbol1, String requiredSymbol2,
                                  ChallengeStatus status, Instant createdAt, Instant expiresAt,
                                  Instant lastCheckedAt, int checkCount,
                                  String baselineSymbols) {
        this.id = Objects.requireNonNull(id);
        this.characterId = Objects.requireNonNull(characterId);
        this.requiredSymbol1 = Objects.requireNonNull(requiredSymbol1);
        this.requiredSymbol2 = Objects.requireNonNull(requiredSymbol2);
        this.baselineSymbols = baselineSymbols;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.lastCheckedAt = lastCheckedAt;
        this.checkCount = checkCount;
    }

    /**
     * 새 챌린지 생성
     */
    public static VerificationChallenge create(CharacterId characterId,
                                               String requiredSymbol1, String requiredSymbol2,
                                               String baselineSymbols) {
        return new VerificationChallenge(
                VerificationChallengeId.generate(),
                characterId,
                requiredSymbol1,
                requiredSymbol2,
                baselineSymbols
        );
    }

    /**
     * 복원용 팩토리
     */
    public static VerificationChallenge reconstitute(
            VerificationChallengeId id, CharacterId characterId,
            String requiredSymbol1, String requiredSymbol2,
            ChallengeStatus status, Instant createdAt, Instant expiresAt,
            Instant lastCheckedAt, int checkCount,
            String baselineSymbols) {
        return new VerificationChallenge(
                id, characterId, requiredSymbol1, requiredSymbol2,
                status, createdAt, expiresAt, lastCheckedAt, checkCount, baselineSymbols
        );
    }

    /**
     * 검사 가능 여부 확인
     */
    public boolean canCheck() {
        if (status != ChallengeStatus.PENDING) {
            return false;
        }
        if (isExpired()) {
            return false;
        }
        if (checkCount >= MAX_CHECK_COUNT) {
            return false;
        }
        if (lastCheckedAt != null) {
            Duration sinceLast = Duration.between(lastCheckedAt, Instant.now());
            Duration effectiveInterval = MIN_CHECK_INTERVAL.minus(CHECK_INTERVAL_TOLERANCE);
            return sinceLast.compareTo(effectiveInterval) >= 0;
        }
        return true;
    }

    /**
     * 다음 검사 가능 시간까지 남은 시간 (초)
     */
    public long getSecondsUntilNextCheck() {
        if (lastCheckedAt == null) {
            return 0;
        }
        Instant nextCheckTime = lastCheckedAt.plus(MIN_CHECK_INTERVAL);
        long seconds = Duration.between(Instant.now(), nextCheckTime).getSeconds();
        return Math.max(0, seconds);
    }

    /**
     * 만료 여부
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 검사 실행 전 유효성 체크
     */
    public void validateForCheck() {
        if (status != ChallengeStatus.PENDING) {
            throw new DomainException("VERIFICATION_INVALID_STATUS",
                    "이미 완료된 챌린지입니다.", Map.of("status", status));
        }

        if (isExpired()) {
            this.status = ChallengeStatus.EXPIRED;
            throw new DomainException("VERIFICATION_EXPIRED",
                    "인증 시간이 만료되었습니다.");
        }

        if (checkCount >= MAX_CHECK_COUNT) {
            this.status = ChallengeStatus.FAILED;
            throw new DomainException("VERIFICATION_MAX_CHECKS",
                    "최대 검사 횟수(10회)를 초과했습니다.",
                    Map.of("checkCount", checkCount, "maxChecks", MAX_CHECK_COUNT));
        }

        if (lastCheckedAt != null) {
            Duration sinceLast = Duration.between(lastCheckedAt, Instant.now());
            // 네트워크 지연을 고려하여 약간의 여유를 둠
            Duration effectiveInterval = MIN_CHECK_INTERVAL.minus(CHECK_INTERVAL_TOLERANCE);
            if (sinceLast.compareTo(effectiveInterval) < 0) {
                long waitSeconds = effectiveInterval.minus(sinceLast).getSeconds();
                throw new DomainException("VERIFICATION_RATE_LIMITED",
                        "잠시 후 다시 시도해주세요.",
                        Map.of("waitSeconds", waitSeconds,
                                "nextCheckAt", lastCheckedAt.plus(effectiveInterval)));
            }
        }
    }

    /**
     * 심볼 검사 결과 처리
     *
     * @param currentSymbols 현재 장착된 심볼 이름 목록
     * @return 검증 결과
     */
    public VerificationResult processCheck(Set<String> currentSymbols) {
        this.lastCheckedAt = Instant.now();
        this.checkCount++;

        Set<String> baselineSet = parseBaselineSymbols();

        // 요구된 심볼 두 개가 해제되었는지 확인
        boolean symbol1Removed = !currentSymbols.contains(requiredSymbol1);
        boolean symbol2Removed = !currentSymbols.contains(requiredSymbol2);

        // 베이스라인에서 해제된 심볼들 계산
        Set<String> removedSymbols = baselineSet.stream()
                .filter(s -> !currentSymbols.contains(s))
                .collect(Collectors.toSet());

        if (symbol1Removed && symbol2Removed) {
            // 정확히 두 개만 해제되었는지 확인
            if (removedSymbols.size() == 2 &&
                    removedSymbols.contains(requiredSymbol1) &&
                    removedSymbols.contains(requiredSymbol2)) {
                this.status = ChallengeStatus.SUCCESS;
                return VerificationResult.success();
            } else if (removedSymbols.size() > 2) {
                // 너무 많이 해제됨
                return VerificationResult.tooManyRemoved(
                        "지정된 심볼 외에 다른 심볼도 해제되었습니다. 정확히 지정된 2개만 해제해주세요.",
                        getRemainingChecks(),
                        getRemainingTime()
                );
            } else {
                // 아직 API에 반영되지 않음
                this.status = ChallengeStatus.SUCCESS;
                return VerificationResult.success();
            }
        }

        // 아직 반영되지 않음
        StringBuilder message = new StringBuilder("심볼 해제가 확인되지 않았습니다:");
        if (!symbol1Removed) {
            message.append(" ≪").append(requiredSymbol1).append("≫");
        }
        if (!symbol2Removed) {
            message.append(" ≪").append(requiredSymbol2).append("≫");
        }

        return VerificationResult.notYet(
                message.toString(),
                getRemainingChecks(),
                getRemainingTime()
        );
    }

    private Set<String> parseBaselineSymbols() {
        if (baselineSymbols == null || baselineSymbols.isBlank()) {
            return Set.of();
        }
        return Set.of(baselineSymbols.split(","));
    }

    /**
     * 만료 처리
     */
    public void expire() {
        if (this.status == ChallengeStatus.PENDING) {
            this.status = ChallengeStatus.EXPIRED;
        }
    }

    /**
     * 실패 처리
     */
    public void fail() {
        if (this.status == ChallengeStatus.PENDING) {
            this.status = ChallengeStatus.FAILED;
        }
    }

    public int getRemainingChecks() {
        return MAX_CHECK_COUNT - checkCount;
    }

    public Duration getRemainingTime() {
        return Duration.between(Instant.now(), expiresAt);
    }

    // Getters
    public VerificationChallengeId getId() {
        return id;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public String getRequiredSymbol1() {
        return requiredSymbol1;
    }

    public String getRequiredSymbol2() {
        return requiredSymbol2;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public int getCheckCount() {
        return checkCount;
    }

    public int getMaxChecks() {
        return MAX_CHECK_COUNT;
    }

    public String getBaselineSymbols() {
        return baselineSymbols;
    }

    // Compatibility getters for existing code
    @Deprecated
    public String getRequiredItemName() {
        return requiredSymbol1 + ", " + requiredSymbol2;
    }

    @Deprecated
    public String getRequiredSlot() {
        return "symbol";
    }

    @Deprecated
    public String getBaselineEquipmentHash() {
        return null;
    }

    @Deprecated
    public String getBaselineSlotItem() {
        return baselineSymbols;
    }

    /**
     * 검증 결과 값 객체
     */
    public record VerificationResult(
            Status status,
            String message,
            Integer remainingChecks,
            Duration remainingTime
    ) {
        public static VerificationResult success() {
            return new VerificationResult(Status.SUCCESS, "인증이 완료되었습니다!", null, null);
        }

        public static VerificationResult notYet(String message, int remainingChecks, Duration remainingTime) {
            return new VerificationResult(Status.NOT_YET, message, remainingChecks, remainingTime);
        }

        public static VerificationResult tooManyRemoved(String message, int remainingChecks, Duration remainingTime) {
            return new VerificationResult(Status.TOO_MANY_REMOVED, message, remainingChecks, remainingTime);
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public enum Status {
            SUCCESS, NOT_YET, TOO_MANY_REMOVED, EXPIRED, MAX_CHECKS_EXCEEDED
        }
    }
}
