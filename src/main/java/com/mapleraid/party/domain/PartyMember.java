package com.mapleraid.party.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.user.domain.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * 파티룸 멤버 (Value Object)
 */
public class PartyMember {
    private final Long persistenceId; // JPA 엔티티 ID 보존용
    private final UserId userId;
    private final CharacterId characterId;
    private boolean isLeader;
    private boolean isReady;
    private Instant readyAt;
    private Instant joinedAt;
    private Instant leftAt;
    private int unreadCount;

    public PartyMember(UserId userId, CharacterId characterId, boolean isLeader) {
        this.persistenceId = null;
        this.userId = Objects.requireNonNull(userId);
        this.characterId = Objects.requireNonNull(characterId);
        this.isLeader = isLeader;
        this.isReady = false;
        this.joinedAt = Instant.now();
        this.unreadCount = 0;
    }

    // 내부 생성자 (reconstitute용)
    private PartyMember(Long persistenceId, UserId userId, CharacterId characterId,
                        boolean isLeader, boolean isReady, Instant readyAt,
                        Instant joinedAt, Instant leftAt, int unreadCount) {
        this.persistenceId = persistenceId;
        this.userId = Objects.requireNonNull(userId);
        this.characterId = Objects.requireNonNull(characterId);
        this.isLeader = isLeader;
        this.isReady = isReady;
        this.readyAt = readyAt;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.unreadCount = unreadCount;
    }

    public static PartyMember reconstitute(
            Long persistenceId,
            UserId userId, CharacterId characterId, boolean isLeader,
            boolean isReady, Instant readyAt,
            Instant joinedAt, Instant leftAt, int unreadCount) {
        PartyMember member = new PartyMember(userId, characterId, isLeader);
        // persistenceId는 final이므로 리플렉션 없이 새 객체 생성 패턴 사용
        return new PartyMember(persistenceId, userId, characterId, isLeader,
                isReady, readyAt, joinedAt, leftAt, unreadCount);
    }

    public void markReady() {
        this.isReady = true;
        this.readyAt = Instant.now();
    }

    public void unmarkReady() {
        this.isReady = false;
        this.readyAt = null;
    }

    public void leave() {
        this.leftAt = Instant.now();
    }

    public boolean hasLeft() {
        return leftAt != null;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void clearUnreadCount() {
        this.unreadCount = 0;
    }

    // Getters
    public Long getPersistenceId() {
        return persistenceId;
    }

    public UserId getUserId() {
        return userId;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public boolean isReady() {
        return isReady;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
