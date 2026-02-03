package com.mapleraid.domain.party;

import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * PartyRoom Aggregate Root - 파티룸
 */
public class PartyRoom {

    private final PartyRoomId id;
    private final PostId postId;
    // Members
    private final List<PartyMember> members = new ArrayList<>();
    private List<String> bossIds = new ArrayList<>();
    private PartyRoomStatus status;
    // Schedule
    private Instant scheduledTime;
    private boolean scheduleConfirmed;
    // Ready Check
    private Instant readyCheckStartedAt;
    private boolean allReady;
    // Timestamps
    private Instant createdAt;
    private Instant completedAt;

    private PartyRoom(PartyRoomId id, PostId postId, List<String> bossIds) {
        this.id = Objects.requireNonNull(id);
        this.postId = Objects.requireNonNull(postId);
        this.bossIds = bossIds != null ? new ArrayList<>(bossIds) : new ArrayList<>();
        this.status = PartyRoomStatus.ACTIVE;
        this.scheduleConfirmed = false;
        this.allReady = false;
        this.createdAt = Instant.now();
    }

    public static PartyRoom create(PostId postId, List<String> bossIds,
                                   UserId leaderId, CharacterId leaderCharacterId) {
        PartyRoom room = new PartyRoom(PartyRoomId.generate(), postId, bossIds);
        room.members.add(new PartyMember(leaderId, leaderCharacterId, true));
        return room;
    }

    public static PartyRoom reconstitute(
            PartyRoomId id, PostId postId, List<String> bossIds,
            PartyRoomStatus status, Instant scheduledTime, boolean scheduleConfirmed,
            Instant readyCheckStartedAt, boolean allReady,
            Instant createdAt, Instant completedAt, List<PartyMember> members) {
        PartyRoom room = new PartyRoom(id, postId, bossIds);
        room.status = status;
        room.scheduledTime = scheduledTime;
        room.scheduleConfirmed = scheduleConfirmed;
        room.readyCheckStartedAt = readyCheckStartedAt;
        room.allReady = allReady;
        room.createdAt = createdAt;
        room.completedAt = completedAt;
        room.members.addAll(members);
        return room;
    }

    /**
     * 멤버 추가
     */
    public void addMember(UserId userId, CharacterId characterId) {
        validateActive();

        boolean alreadyMember = members.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
        if (alreadyMember) {
            throw new DomainException("PARTY_ALREADY_MEMBER",
                    "이미 파티룸의 멤버입니다.");
        }

        members.add(new PartyMember(userId, characterId, false));
    }

    /**
     * 멤버 탈퇴
     */
    public void removeMember(UserId userId) {
        validateActive();

        PartyMember member = findActiveMember(userId);

        if (member.isLeader()) {
            throw new DomainException("PARTY_LEADER_CANNOT_LEAVE",
                    "파티장은 탈퇴할 수 없습니다. 파티를 취소하거나 리더를 위임해주세요.");
        }

        member.leave();
    }

    /**
     * 레디 체크 시작
     */
    public void startReadyCheck(UserId requesterId) {
        validateActive();
        validateLeader(requesterId);

        this.readyCheckStartedAt = Instant.now();
        this.allReady = false;

        // 모든 멤버 레디 초기화
        members.stream()
                .filter(PartyMember::isActive)
                .forEach(PartyMember::unmarkReady);
    }

    /**
     * 레디 표시
     */
    public void markReady(UserId userId) {
        validateActive();

        if (readyCheckStartedAt == null) {
            throw new DomainException("PARTY_NO_READY_CHECK",
                    "레디 체크가 시작되지 않았습니다.");
        }

        PartyMember member = findActiveMember(userId);
        member.markReady();

        // 모든 활성 멤버가 레디인지 확인
        this.allReady = members.stream()
                .filter(PartyMember::isActive)
                .allMatch(PartyMember::isReady);
    }

    /**
     * 파티 완료
     */
    public void complete(UserId requesterId) {
        validateActive();

        // 리더이거나 과반수 동의 필요 (MVP에서는 리더만)
        validateLeader(requesterId);

        this.status = PartyRoomStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    /**
     * 파티 취소
     */
    public void cancel(UserId requesterId) {
        validateActive();
        validateLeader(requesterId);

        this.status = PartyRoomStatus.CANCELED;
        this.completedAt = Instant.now();
    }

    /**
     * 일정 설정
     */
    public void setSchedule(Instant scheduledTime) {
        validateActive();
        this.scheduledTime = scheduledTime;
        this.scheduleConfirmed = true;
    }

    private void validateActive() {
        if (status != PartyRoomStatus.ACTIVE) {
            throw new DomainException("PARTY_NOT_ACTIVE",
                    "활성 상태의 파티룸이 아닙니다.");
        }
    }

    private void validateLeader(UserId userId) {
        boolean isLeader = members.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isLeader() && m.isActive());
        if (!isLeader) {
            throw new DomainException("PARTY_NOT_LEADER",
                    "파티장만 이 작업을 수행할 수 있습니다.");
        }
    }

    private PartyMember findActiveMember(UserId userId) {
        return members.stream()
                .filter(m -> m.getUserId().equals(userId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new DomainException("PARTY_NOT_MEMBER",
                        "해당 파티룸의 멤버가 아닙니다."));
    }

    public boolean isMember(UserId userId) {
        return members.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
    }

    public boolean isLeader(UserId userId) {
        return members.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isLeader() && m.isActive());
    }

    public List<PartyMember> getActiveMembers() {
        return members.stream()
                .filter(PartyMember::isActive)
                .toList();
    }

    public List<UserId> getActiveMemberIds() {
        return getActiveMembers().stream()
                .map(PartyMember::getUserId)
                .toList();
    }

    public int getMemberCount() {
        return (int) members.stream().filter(PartyMember::isActive).count();
    }

    public boolean isCompleted() {
        return status == PartyRoomStatus.COMPLETED;
    }

    /**
     * 새 메시지가 왔을 때 발신자 외 모든 멤버의 unreadCount 증가
     */
    public void incrementUnreadCountExcept(UserId senderId) {
        members.stream()
                .filter(PartyMember::isActive)
                .filter(m -> !m.getUserId().equals(senderId))
                .forEach(PartyMember::incrementUnreadCount);
    }

    /**
     * 특정 사용자의 unreadCount 초기화 (읽음 처리)
     */
    public void clearUnreadCount(UserId userId) {
        members.stream()
                .filter(m -> m.getUserId().equals(userId) && m.isActive())
                .findFirst()
                .ifPresent(PartyMember::clearUnreadCount);
    }

    /**
     * 특정 사용자의 unreadCount 조회
     */
    public int getUnreadCount(UserId userId) {
        return members.stream()
                .filter(m -> m.getUserId().equals(userId) && m.isActive())
                .findFirst()
                .map(PartyMember::getUnreadCount)
                .orElse(0);
    }

    // Getters
    public PartyRoomId getId() {
        return id;
    }

    public PostId getPostId() {
        return postId;
    }

    public List<String> getBossIds() {
        return Collections.unmodifiableList(bossIds);
    }

    public PartyRoomStatus getStatus() {
        return status;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public boolean isScheduleConfirmed() {
        return scheduleConfirmed;
    }

    public Instant getReadyCheckStartedAt() {
        return readyCheckStartedAt;
    }

    public boolean isAllReady() {
        return allReady;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<PartyMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
