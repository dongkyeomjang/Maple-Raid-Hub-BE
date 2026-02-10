package com.mapleraid.user.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * User Aggregate Root
 */
@Getter
public class User {
    private static final BigDecimal DEFAULT_TEMPERATURE = new BigDecimal("36.5");

    private final UserId id;
    private final String username;
    private final String passwordHash;
    private String nickname;

    // OAuth
    private String provider;      // "default", "kakao", etc.
    private String providerId;    // OAuth provider's user ID
    private boolean nicknameSet;  // 닉네임 설정 완료 여부

    // Temperature & Reputation
    private BigDecimal temperature;
    private int completedPartyCount;
    private int noShowCount;

    // Status
    private UserStatus status;
    private Instant suspendedUntil;

    // Discord
    private String discordId;
    private String discordUsername;
    private boolean discordNotificationsEnabled;
    private boolean discordPromptDismissed;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private User(UserId id, String username, String passwordHash, String nickname) {
        this.id = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.passwordHash = passwordHash; // nullable for OAuth users
        this.nickname = Objects.requireNonNull(nickname);
        this.provider = "default";
        this.nicknameSet = true; // 일반 회원가입은 닉네임 설정 완료
        this.temperature = DEFAULT_TEMPERATURE;
        this.completedPartyCount = 0;
        this.noShowCount = 0;
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static User create(UserId id, String username, String passwordHash, String nickname) {
        return new User(id, username, passwordHash, nickname);
    }

    /**
     * OAuth 사용자 생성 (임시 닉네임, 랜덤 비밀번호 해시)
     */
    public static User createOAuthUser(UserId id, String provider, String providerId, String tempNickname, String randomPasswordHash) {
        User user = new User(id, providerId, randomPasswordHash, tempNickname);
        user.provider = provider;
        user.providerId = providerId;
        user.nicknameSet = false; // OAuth는 닉네임 설정 필요
        return user;
    }

    public static User reconstitute(
            UserId id, String username, String passwordHash, String nickname,
            BigDecimal temperature, int completedPartyCount, int noShowCount,
            UserStatus status, Instant suspendedUntil,
            Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        return reconstitute(id, username, passwordHash, nickname, "default", null, true,
                temperature, completedPartyCount, noShowCount, status, suspendedUntil,
                createdAt, updatedAt, lastLoginAt);
    }

    public static User reconstitute(
            UserId id, String username, String passwordHash, String nickname,
            String provider, String providerId, boolean nicknameSet,
            BigDecimal temperature, int completedPartyCount, int noShowCount,
            UserStatus status, Instant suspendedUntil,
            Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        return reconstitute(id, username, passwordHash, nickname, provider, providerId, nicknameSet,
                temperature, completedPartyCount, noShowCount, status, suspendedUntil,
                createdAt, updatedAt, lastLoginAt,
                null, null, false, false);
    }

    public static User reconstitute(
            UserId id, String username, String passwordHash, String nickname,
            String provider, String providerId, boolean nicknameSet,
            BigDecimal temperature, int completedPartyCount, int noShowCount,
            UserStatus status, Instant suspendedUntil,
            Instant createdAt, Instant updatedAt, Instant lastLoginAt,
            String discordId, String discordUsername,
            boolean discordNotificationsEnabled, boolean discordPromptDismissed) {
        User user = new User(id, username, passwordHash, nickname);
        user.provider = provider != null ? provider : "default";
        user.providerId = providerId;
        user.nicknameSet = nicknameSet;
        user.temperature = temperature;
        user.completedPartyCount = completedPartyCount;
        user.noShowCount = noShowCount;
        user.status = status;
        user.suspendedUntil = suspendedUntil;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.lastLoginAt = lastLoginAt;
        user.discordId = discordId;
        user.discordUsername = discordUsername;
        user.discordNotificationsEnabled = discordNotificationsEnabled;
        user.discordPromptDismissed = discordPromptDismissed;
        return user;
    }

    /**
     * 온도 조정
     */
    public void adjustTemperature(BigDecimal delta) {
        this.temperature = this.temperature.add(delta);
        // 범위 제한 (0 ~ 100)
        if (this.temperature.compareTo(BigDecimal.ZERO) < 0) {
            this.temperature = BigDecimal.ZERO;
        }
        if (this.temperature.compareTo(new BigDecimal("100")) > 0) {
            this.temperature = new BigDecimal("100");
        }
        this.updatedAt = Instant.now();
    }

    /**
     * 파티 완료 기록
     */
    public void recordPartyCompletion() {
        this.completedPartyCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * 노쇼 기록
     */
    public void recordNoShow() {
        this.noShowCount++;
        this.adjustTemperature(new BigDecimal("-2.0"));
        this.updatedAt = Instant.now();
    }

    /**
     * 노쇼율 계산
     */
    public BigDecimal getNoShowRate() {
        int totalParticipation = completedPartyCount + noShowCount;
        if (totalParticipation == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(noShowCount)
                .divide(new BigDecimal(totalParticipation), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 로그인 기록
     */
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 계정 정지
     */
    public void suspend(Instant until) {
        this.status = UserStatus.SUSPENDED;
        this.suspendedUntil = until;
        this.updatedAt = Instant.now();
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        if (status == UserStatus.BANNED) {
            return false;
        }
        if (status == UserStatus.SUSPENDED && suspendedUntil != null) {
            if (Instant.now().isBefore(suspendedUntil)) {
                return false;
            }
            // 정지 기간 만료 시 자동 해제
            this.status = UserStatus.ACTIVE;
            this.suspendedUntil = null;
        }
        return status == UserStatus.ACTIVE;
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
        this.nicknameSet = true;
        this.updatedAt = Instant.now();
    }

    /**
     * OAuth 사용자인지 확인
     */
    public boolean isOAuthUser() {
        return provider != null && !"default".equals(provider);
    }

    /**
     * Discord 연동
     */
    public void linkDiscord(String discordId, String discordUsername) {
        this.discordId = Objects.requireNonNull(discordId);
        this.discordUsername = Objects.requireNonNull(discordUsername);
        this.discordNotificationsEnabled = true;
        this.discordPromptDismissed = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Discord 연동 해제
     */
    public void unlinkDiscord() {
        this.discordId = null;
        this.discordUsername = null;
        this.discordNotificationsEnabled = false;
        this.updatedAt = Instant.now();
    }

    public boolean isDiscordLinked() {
        return discordId != null;
    }

    public boolean isDiscordNotificationsEnabled() {
        return discordNotificationsEnabled;
    }

    public void dismissDiscordPrompt() {
        this.discordPromptDismissed = true;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isNicknameSet() {
        return nicknameSet;
    }

    public double getTemperature() {
        return temperature.doubleValue();
    }

    public BigDecimal getTemperatureDecimal() {
        return temperature;
    }

    public int getCompletedParties() {
        return completedPartyCount;
    }

    public int getCompletedPartyCount() {
        return completedPartyCount;
    }

    public int getNoShowCount() {
        return noShowCount;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getSuspendedUntil() {
        return suspendedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getDiscordUsername() {
        return discordUsername;
    }

    public boolean isDiscordPromptDismissed() {
        return discordPromptDismissed;
    }

    public enum UserStatus {
        ACTIVE, SUSPENDED, BANNED
    }
}
