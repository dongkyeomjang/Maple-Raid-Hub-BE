package com.mapleraid.user.adapter.out.persistence.jpa;

import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "provider", length = 20, columnDefinition = "varchar(20) default 'default'")
    private String provider = "default";

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "nickname_set", columnDefinition = "boolean default true")
    private Boolean nicknameSet = true;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "completed_party_count")
    private int completedPartyCount;

    @Column(name = "no_show_count")
    private int noShowCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private User.UserStatus status;

    @Column(name = "suspended_until")
    private Instant suspendedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "discord_id", length = 30)
    private String discordId;

    @Column(name = "discord_username", length = 100)
    private String discordUsername;

    @Column(name = "discord_notifications_enabled", columnDefinition = "boolean default false")
    private boolean discordNotificationsEnabled;

    @Column(name = "discord_prompt_dismissed", columnDefinition = "boolean default false")
    private boolean discordPromptDismissed;

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.id = user.getId().getValue().toString();
        entity.username = user.getUsername();
        entity.passwordHash = user.getPasswordHash();
        entity.nickname = user.getNickname();
        entity.provider = user.getProvider();
        entity.providerId = user.getProviderId();
        entity.nicknameSet = user.isNicknameSet();
        entity.temperature = user.getTemperatureDecimal();
        entity.completedPartyCount = user.getCompletedPartyCount();
        entity.noShowCount = user.getNoShowCount();
        entity.status = user.getStatus();
        entity.suspendedUntil = user.getSuspendedUntil();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        entity.lastLoginAt = user.getLastLoginAt();
        entity.discordId = user.getDiscordId();
        entity.discordUsername = user.getDiscordUsername();
        entity.discordNotificationsEnabled = user.isDiscordNotificationsEnabled();
        entity.discordPromptDismissed = user.isDiscordPromptDismissed();
        return entity;
    }

    public User toDomain() {
        return User.reconstitute(
                UserId.of(id),
                username,
                passwordHash,
                nickname,
                provider != null ? provider : "default",
                providerId,
                nicknameSet != null ? nicknameSet : true,
                temperature,
                completedPartyCount,
                noShowCount,
                status,
                suspendedUntil,
                createdAt,
                updatedAt,
                lastLoginAt,
                discordId,
                discordUsername,
                discordNotificationsEnabled,
                discordPromptDismissed
        );
    }
}
