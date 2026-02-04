package com.mapleraid.post.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Post Aggregate Root - 파티 모집글
 */
public class Post {

    private static final Duration DEFAULT_EXPIRY = Duration.ofDays(7);

    private final PostId id;
    private final UserId authorId;
    private final CharacterId characterId;
    // World Group
    private final EWorldGroup EWorldGroup;
    // Applications (part of aggregate)
    private final List<Application> applications = new ArrayList<>();
    // Target Bosses (사용자가 선택한 보스 목록)
    private List<String> bossIds = new ArrayList<>();
    // Recruitment Info
    private int requiredMembers;
    private int currentMembers;
    private String preferredTime;
    private String description;
    // Status
    private PostStatus status;
    private PartyRoomId partyRoomId;
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private Instant closedAt;

    private Post(PostId id, UserId authorId, CharacterId characterId, EWorldGroup EWorldGroup) {
        this.id = Objects.requireNonNull(id);
        this.authorId = Objects.requireNonNull(authorId);
        this.characterId = Objects.requireNonNull(characterId);
        this.EWorldGroup = Objects.requireNonNull(EWorldGroup);
        this.status = PostStatus.RECRUITING;
        this.currentMembers = 1; // 작성자 포함
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.expiresAt = createdAt.plus(DEFAULT_EXPIRY);
    }

    public static Post create(UserId authorId, CharacterId characterId, EWorldGroup EWorldGroup,
                              List<String> bossIds, int requiredMembers,
                              String preferredTime, String description) {
        // 최소 1개 이상의 보스 선택 필수
        if (bossIds == null || bossIds.isEmpty()) {
            throw new CommonException(ErrorCode.POST_NO_BOSS_SELECTED);
        }

        if (requiredMembers < 2 || requiredMembers > 6) {
            throw new CommonException(ErrorCode.POST_INVALID_MEMBER_COUNT);
        }

        Post post = new Post(PostId.generate(), authorId, characterId, EWorldGroup);
        post.bossIds = new ArrayList<>(bossIds);
        post.requiredMembers = requiredMembers;
        post.preferredTime = preferredTime;
        post.description = description;
        return post;
    }

    public static Post reconstitute(
            PostId id, UserId authorId, CharacterId characterId, EWorldGroup EWorldGroup,
            List<String> bossIds, int requiredMembers, int currentMembers,
            String preferredTime, String description,
            PostStatus status, PartyRoomId partyRoomId,
            Instant createdAt, Instant updatedAt, Instant expiresAt, Instant closedAt,
            List<Application> applications) {
        Post post = new Post(id, authorId, characterId, EWorldGroup);
        post.bossIds = bossIds != null ? new ArrayList<>(bossIds) : new ArrayList<>();
        post.requiredMembers = requiredMembers;
        post.currentMembers = currentMembers;
        post.preferredTime = preferredTime;
        post.description = description;
        post.status = status;
        post.partyRoomId = partyRoomId;
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        post.expiresAt = expiresAt;
        post.closedAt = closedAt;
        if (applications != null) {
            post.applications.addAll(applications);
        }
        return post;
    }

    /**
     * 지원 추가
     */
    public Application apply(UserId applicantId, CharacterId applicantCharacterId,
                             EWorldGroup applicantEWorldGroup, String message) {
        // 본인 모집글에 지원 불가
        if (authorId.equals(applicantId)) {
            throw new CommonException(ErrorCode.APPLICATION_SELF_APPLY);
        }

        // 월드 그룹 확인
        if (EWorldGroup != applicantEWorldGroup) {
            throw new CommonException(ErrorCode.APPLICATION_WORLD_GROUP_MISMATCH);
        }

        // 모집 상태 확인
        if (status != PostStatus.RECRUITING) {
            throw new CommonException(ErrorCode.APPLICATION_POST_NOT_RECRUITING);
        }

        // 중복 지원 확인 (거절된 경우 재지원 허용)
        boolean alreadyApplied = applications.stream()
                .anyMatch(app -> app.getApplicantId().equals(applicantId)
                        && app.getStatus() != ApplicationStatus.WITHDRAWN
                        && app.getStatus() != ApplicationStatus.CANCELED
                        && app.getStatus() != ApplicationStatus.REJECTED);
        if (alreadyApplied) {
            throw new CommonException(ErrorCode.APPLICATION_DUPLICATE);
        }

        Application application = Application.create(id, applicantId, applicantCharacterId, message);
        applications.add(application);
        updatedAt = Instant.now();

        return application;
    }

    /**
     * 지원 수락
     */
    public void acceptApplication(ApplicationId applicationId) {
        Application application = findApplication(applicationId);
        application.accept();

        currentMembers++;
        updatedAt = Instant.now();

        // 인원 충족 시 자동 마감
        if (currentMembers >= requiredMembers) {
            close();
        }
    }

    /**
     * 지원 거절
     */
    public void rejectApplication(ApplicationId applicationId) {
        Application application = findApplication(applicationId);
        application.reject();
        updatedAt = Instant.now();
    }

    /**
     * 지원 취소 (지원자가 직접)
     */
    public void withdrawApplication(ApplicationId applicationId, UserId requesterId) {
        Application application = findApplication(applicationId);

        if (!application.getApplicantId().equals(requesterId)) {
            throw new CommonException(ErrorCode.APPLICATION_NOT_OWNER);
        }

        application.withdraw();
        updatedAt = Instant.now();
    }

    /**
     * 모집 마감 (현재 인원으로 파티 결성)
     */
    public void close() {
        if (status != PostStatus.RECRUITING) {
            throw new CommonException(ErrorCode.POST_CANNOT_CLOSE);
        }
        if (currentMembers < 2) {
            throw new CommonException(ErrorCode.POST_INSUFFICIENT_MEMBERS);
        }
        this.status = PostStatus.CLOSED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();

        // 대기 중인 모든 지원 거절 처리
        applications.stream()
                .filter(Application::isPending)
                .forEach(Application::reject);
    }

    /**
     * 모집글 취소
     */
    public void cancel() {
        if (partyRoomId != null) {
            throw new CommonException(ErrorCode.POST_HAS_PARTY_ROOM);
        }

        this.status = PostStatus.CANCELED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();

        // 대기 중인 모든 지원 취소
        applications.stream()
                .filter(Application::isPending)
                .forEach(Application::cancelByPost);
    }

    /**
     * 파티룸 연결
     */
    public void linkPartyRoom(PartyRoomId partyRoomId) {
        this.partyRoomId = partyRoomId;
        this.updatedAt = Instant.now();
    }

    /**
     * 수정
     */
    public void update(List<String> bossIds, Integer requiredMembers,
                       String preferredTime, boolean clearPreferredTime,
                       String description, boolean clearDescription) {
        if (status != PostStatus.RECRUITING) {
            throw new CommonException(ErrorCode.POST_NOT_EDITABLE);
        }

        if (bossIds != null) {
            if (bossIds.isEmpty()) {
                throw new CommonException(ErrorCode.POST_NO_BOSS_SELECTED);
            }
            this.bossIds = new ArrayList<>(bossIds);
        }

        if (requiredMembers != null) {
            if (requiredMembers < 2 || requiredMembers > 6) {
                throw new CommonException(ErrorCode.POST_INVALID_MEMBER_COUNT);
            }
            if (requiredMembers < currentMembers) {
                throw new CommonException(ErrorCode.POST_MEMBER_COUNT_BELOW_CURRENT);
            }
            this.requiredMembers = requiredMembers;
        }

        if (clearPreferredTime) {
            this.preferredTime = null;
        } else if (preferredTime != null) {
            this.preferredTime = preferredTime;
        }

        if (clearDescription) {
            this.description = null;
        } else if (description != null) {
            this.description = description;
        }

        this.updatedAt = Instant.now();
    }

    /**
     * 만료 체크 및 처리
     */
    public boolean checkAndExpire() {
        if (status == PostStatus.RECRUITING && Instant.now().isAfter(expiresAt)) {
            this.status = PostStatus.EXPIRED;
            this.closedAt = Instant.now();
            applications.stream()
                    .filter(Application::isPending)
                    .forEach(Application::cancelByPost);
            return true;
        }
        return false;
    }

    private Application findApplication(ApplicationId applicationId) {
        return applications.stream()
                .filter(app -> app.getId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new CommonException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    public List<Application> getAcceptedApplications() {
        return applications.stream()
                .filter(Application::isAccepted)
                .toList();
    }

    public boolean isRecruiting() {
        return status == PostStatus.RECRUITING;
    }

    public boolean isAuthor(UserId userId) {
        return authorId.equals(userId);
    }

    // Getters
    public PostId getId() {
        return id;
    }

    public UserId getAuthorId() {
        return authorId;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public List<String> getBossIds() {
        return Collections.unmodifiableList(bossIds);
    }

    public EWorldGroup getWorldGroup() {
        return EWorldGroup;
    }

    public int getRequiredMembers() {
        return requiredMembers;
    }

    public int getCurrentMembers() {
        return currentMembers;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public String getDescription() {
        return description;
    }

    public PostStatus getStatus() {
        return status;
    }

    public PartyRoomId getPartyRoomId() {
        return partyRoomId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public List<Application> getApplications() {
        return Collections.unmodifiableList(applications);
    }
}
