package com.mapleraid.adapter.out.persistence.entity;

import com.mapleraid.domain.party.PartyMember;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.party.PartyRoomStatus;
import com.mapleraid.domain.post.PostId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "party_rooms")
@NamedEntityGraph(
        name = "PartyRoom.withMembersAndBossIds",
        attributeNodes = {
                @NamedAttributeNode("members"),
                @NamedAttributeNode("bossIds")
        }
)
public class PartyRoomJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "post_id", nullable = false, length = 36)
    private String postId;

    @ElementCollection
    @CollectionTable(name = "party_room_boss_ids", joinColumns = @JoinColumn(name = "party_room_id"))
    @Column(name = "boss_id", length = 50)
    private Set<String> bossIds = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PartyRoomStatus status;

    @Column(name = "scheduled_time")
    private Instant scheduledTime;

    @Column(name = "schedule_confirmed")
    private boolean scheduleConfirmed;

    @Column(name = "ready_check_started_at")
    private Instant readyCheckStartedAt;

    @Column(name = "all_ready")
    private boolean allReady;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "partyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMemberJpaEntity> members = new ArrayList<>();

    protected PartyRoomJpaEntity() {
    }

    public static PartyRoomJpaEntity fromDomain(PartyRoom partyRoom) {
        PartyRoomJpaEntity entity = new PartyRoomJpaEntity();
        entity.id = partyRoom.getId().getValue().toString();
        entity.postId = partyRoom.getPostId().getValue().toString();
        entity.bossIds = new HashSet<>(partyRoom.getBossIds());
        entity.status = partyRoom.getStatus();
        entity.scheduledTime = partyRoom.getScheduledTime();
        entity.scheduleConfirmed = partyRoom.isScheduleConfirmed();
        entity.readyCheckStartedAt = partyRoom.getReadyCheckStartedAt();
        entity.allReady = partyRoom.isAllReady();
        entity.createdAt = partyRoom.getCreatedAt();
        entity.completedAt = partyRoom.getCompletedAt();

        entity.members = new ArrayList<>(partyRoom.getMembers().stream()
                .map(member -> PartyMemberJpaEntity.fromDomain(member, entity))
                .toList());

        return entity;
    }

    /**
     * 기존 엔티티를 도메인 객체의 값으로 업데이트 (orphanRemoval 정상 작동)
     */
    public void updateFromDomain(PartyRoom partyRoom) {
        this.bossIds.clear();
        this.bossIds.addAll(partyRoom.getBossIds());
        this.status = partyRoom.getStatus();
        this.scheduledTime = partyRoom.getScheduledTime();
        this.scheduleConfirmed = partyRoom.isScheduleConfirmed();
        this.readyCheckStartedAt = partyRoom.getReadyCheckStartedAt();
        this.allReady = partyRoom.isAllReady();
        this.completedAt = partyRoom.getCompletedAt();

        // 멤버 컬렉션 업데이트 (orphanRemoval 작동하도록 clear + addAll)
        this.members.clear();
        this.members.addAll(partyRoom.getMembers().stream()
                .map(member -> PartyMemberJpaEntity.fromDomain(member, this))
                .toList());
    }

    public PartyRoom toDomain() {
        List<PartyMember> domainMembers = members.stream()
                .map(PartyMemberJpaEntity::toDomain)
                .toList();

        return PartyRoom.reconstitute(
                PartyRoomId.of(id),
                PostId.of(postId),
                new ArrayList<>(bossIds),
                status,
                scheduledTime,
                scheduleConfirmed,
                readyCheckStartedAt,
                allReady,
                createdAt,
                completedAt,
                domainMembers
        );
    }

    public String getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public PartyRoomStatus getStatus() {
        return status;
    }
}
