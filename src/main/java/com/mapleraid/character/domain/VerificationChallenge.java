package com.mapleraid.character.domain;

import com.mapleraid.character.domain.type.EChallengeStatus;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 캐릭터 소유권 인증 챌린지
 * 특정 심볼 두 개를 해제하도록 요청
 */
@Getter
public class VerificationChallenge {

    private static final Duration CHALLENGE_DURATION = Duration.ofMinutes(30);
    private static final Duration MIN_CHECK_INTERVAL = Duration.ofSeconds(30);
    private static final Duration CHECK_INTERVAL_TOLERANCE = Duration.ofSeconds(3);
    private static final int MAX_CHECK_COUNT = 10;

    // Getters
    private final VerificationChallengeId id;
    private final CharacterId characterId;
    private final String requiredSymbol1;
    private final String requiredSymbol2;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final String baselineSymbols;
    private EChallengeStatus status;
    private LocalDateTime lastCheckedAt;
    private int checkCount;

    private VerificationChallenge(VerificationChallengeId id, CharacterId characterId,
                                  String requiredSymbol1, String requiredSymbol2,
                                  String baselineSymbols) {
        this.id = Objects.requireNonNull(id);
        this.characterId = Objects.requireNonNull(characterId);
        this.requiredSymbol1 = Objects.requireNonNull(requiredSymbol1);
        this.requiredSymbol2 = Objects.requireNonNull(requiredSymbol2);
        this.baselineSymbols = baselineSymbols;
        this.status = EChallengeStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plus(CHALLENGE_DURATION);
        this.checkCount = 0;
    }

    private VerificationChallenge(VerificationChallengeId id, CharacterId characterId,
                                  String requiredSymbol1, String requiredSymbol2,
                                  EChallengeStatus status, LocalDateTime createdAt, LocalDateTime expiresAt,
                                  LocalDateTime lastCheckedAt, int checkCount,
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
            EChallengeStatus status, LocalDateTime createdAt, LocalDateTime expiresAt,
            LocalDateTime lastCheckedAt, int checkCount,
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
        if (status != EChallengeStatus.PENDING) {
            return false;
        }
        if (isExpired()) {
            return false;
        }
        if (checkCount >= MAX_CHECK_COUNT) {
            return false;
        }
        if (lastCheckedAt != null) {
            Duration sinceLast = Duration.between(lastCheckedAt, LocalDateTime.now());
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
        LocalDateTime nextCheckTime = lastCheckedAt.plus(MIN_CHECK_INTERVAL);
        long seconds = Duration.between(LocalDateTime.now(), nextCheckTime).getSeconds();
        return Math.max(0, seconds);
    }

    /**
     * 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 검사 실행 전 유효성 체크
     */
    public void validateForCheck() {
        if (status != EChallengeStatus.PENDING) {
            throw new CommonException(ErrorCode.VERIFICATION_INVALID_STATUS);
        }

        if (isExpired()) {
            this.status = EChallengeStatus.EXPIRED;
            throw new CommonException(ErrorCode.VERIFICATION_EXPIRED);
        }

        if (checkCount >= MAX_CHECK_COUNT) {
            this.status = EChallengeStatus.FAILED;
            throw new CommonException(ErrorCode.VERIFICATION_MAX_CHECKS);
        }

        if (lastCheckedAt != null) {
            Duration sinceLast = Duration.between(lastCheckedAt, LocalDateTime.now());
            Duration effectiveInterval = MIN_CHECK_INTERVAL.minus(CHECK_INTERVAL_TOLERANCE);
            if (sinceLast.compareTo(effectiveInterval) < 0) {
                long waitSeconds = effectiveInterval.minus(sinceLast).getSeconds();
                throw new CommonException(ErrorCode.VERIFICATION_RATE_LIMITED);
            }
        }
    }

    /**
     * 심볼 검사 결과 처리
     */
    public VerificationResult processCheck(Set<String> currentSymbols) {
        this.lastCheckedAt = LocalDateTime.now();
        this.checkCount++;

        Set<String> baselineSet = parseBaselineSymbols();

        boolean symbol1Removed = !currentSymbols.contains(requiredSymbol1);
        boolean symbol2Removed = !currentSymbols.contains(requiredSymbol2);

        Set<String> removedSymbols = baselineSet.stream()
                .filter(s -> !currentSymbols.contains(s))
                .collect(Collectors.toSet());

        if (symbol1Removed && symbol2Removed) {
            if (removedSymbols.size() == 2 &&
                    removedSymbols.contains(requiredSymbol1) &&
                    removedSymbols.contains(requiredSymbol2)) {
                this.status = EChallengeStatus.SUCCESS;
                return VerificationResult.success();
            } else if (removedSymbols.size() > 2) {
                return VerificationResult.tooManyRemoved(
                        "지정된 심볼 외에 다른 심볼도 해제되었습니다. 정확히 지정된 2개만 해제해주세요.",
                        getRemainingChecks(),
                        getRemainingTime()
                );
            } else {
                this.status = EChallengeStatus.SUCCESS;
                return VerificationResult.success();
            }
        }

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
        if (this.status == EChallengeStatus.PENDING) {
            this.status = EChallengeStatus.EXPIRED;
        }
    }

    /**
     * 실패 처리
     */
    public void fail() {
        if (this.status == EChallengeStatus.PENDING) {
            this.status = EChallengeStatus.FAILED;
        }
    }

    public int getRemainingChecks() {
        return MAX_CHECK_COUNT - checkCount;
    }

    public Duration getRemainingTime() {
        return Duration.between(LocalDateTime.now(), expiresAt);
    }

    public int getMaxChecks() {
        return MAX_CHECK_COUNT;
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
