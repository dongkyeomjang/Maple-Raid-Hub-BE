package com.mapleraid.party.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "party_members")
public class PartyMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_room_id", nullable = false)
    private PartyRoomJpaEntity partyRoom;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "character_id", nullable = false, length = 36)
    private String characterId;

    @Column(name = "is_leader")
    private boolean isLeader;

    @Column(name = "is_ready")
    private boolean isReady;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount = 0;

    public static PartyMemberJpaEntity fromDomain(PartyMember member, PartyRoomJpaEntity partyRoomEntity) {
        PartyMemberJpaEntity entity = new PartyMemberJpaEntity();
        entity.partyRoom = partyRoomEntity;
        entity.userId = member.getUserId().getValue().toString();
        entity.characterId = member.getCharacterId().getValue().toString();
        entity.isLeader = member.isLeader();
        entity.isReady = member.isReady();
        entity.readyAt = member.getReadyAt();
        entity.joinedAt = member.getJoinedAt();
        entity.leftAt = member.getLeftAt();
        entity.unreadCount = member.getUnreadCount();
        return entity;
    }

    public PartyMember toDomain() {
        return PartyMember.reconstitute(
                id,
                UserId.of(userId),
                CharacterId.of(characterId),
                isLeader,
                isReady,
                readyAt,
                joinedAt,
                leftAt,
                unreadCount
        );
    }
}
