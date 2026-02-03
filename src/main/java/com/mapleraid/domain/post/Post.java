package com.mapleraid.domain.post;

import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.user.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final WorldGroup worldGroup;
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

    private Post(PostId id, UserId authorId, CharacterId characterId, WorldGroup worldGroup) {
        this.id = Objects.requireNonNull(id);
        this.authorId = Objects.requireNonNull(authorId);
        this.characterId = Objects.requireNonNull(characterId);
        this.worldGroup = Objects.requireNonNull(worldGroup);
        this.status = PostStatus.RECRUITING;
        this.currentMembers = 1; // 작성자 포함
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.expiresAt = createdAt.plus(DEFAULT_EXPIRY);
    }

    public static Post create(UserId authorId, CharacterId characterId, WorldGroup worldGroup,
                              List<String> bossIds, int requiredMembers,
                              String preferredTime, String description) {
        // 최소 1개 이상의 보스 선택 필수
        if (bossIds == null || bossIds.isEmpty()) {
            throw new DomainException("POST_NO_BOSS_SELECTED",
                    "최소 1개 이상의 보스를 선택해야 합니다.");
        }

        if (requiredMembers < 2 || requiredMembers > 6) {
            throw new DomainException("POST_INVALID_MEMBER_COUNT",
                    "모집 인원은 2~6명이어야 합니다.");
        }

        Post post = new Post(PostId.generate(), authorId, characterId, worldGroup);
        post.bossIds = new ArrayList<>(bossIds);
        post.requiredMembers = requiredMembers;
        post.preferredTime = preferredTime;
        post.description = description;
        return post;
    }

    public static Post reconstitute(
            PostId id, UserId authorId, CharacterId characterId, WorldGroup worldGroup,
            List<String> bossIds, int requiredMembers, int currentMembers,
            String preferredTime, String description,
            PostStatus status, PartyRoomId partyRoomId,
            Instant createdAt, Instant updatedAt, Instant expiresAt, Instant closedAt,
            List<Application> applications) {
        Post post = new Post(id, authorId, characterId, worldGroup);
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
                             WorldGroup applicantWorldGroup, String message) {
        // 본인 모집글에 지원 불가
        if (authorId.equals(applicantId)) {
            throw new DomainException("APPLICATION_SELF_APPLY",
                    "본인 모집글에는 지원할 수 없습니다.");
        }

        // 월드 그룹 확인
        if (worldGroup != applicantWorldGroup) {
            throw new DomainException("APPLICATION_WORLD_GROUP_MISMATCH",
                    String.format("해당 모집글은 %s 전용입니다. %s 캐릭터로는 지원할 수 없습니다.",
                            worldGroup.getDisplayName(), applicantWorldGroup.getDisplayName()),
                    Map.of(
                            "postWorldGroup", worldGroup,
                            "characterWorldGroup", applicantWorldGroup,
                            "allowedGroups", List.of(worldGroup)
                    ));
        }

        // 모집 상태 확인
        if (status != PostStatus.RECRUITING) {
            throw new DomainException("APPLICATION_POST_NOT_RECRUITING",
                    "모집이 마감된 모집글입니다.");
        }

        // 중복 지원 확인
        boolean alreadyApplied = applications.stream()
                .anyMatch(app -> app.getApplicantId().equals(applicantId)
                        && app.getStatus() != ApplicationStatus.WITHDRAWN
                        && app.getStatus() != ApplicationStatus.CANCELED);
        if (alreadyApplied) {
            throw new DomainException("APPLICATION_DUPLICATE",
                    "이미 지원한 모집글입니다.");
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
            throw new DomainException("APPLICATION_NOT_OWNER",
                    "본인의 지원만 취소할 수 있습니다.");
        }

        application.withdraw();
        updatedAt = Instant.now();
    }

    /**
     * 모집 마감 (현재 인원으로 파티 결성)
     */
    public void close() {
        if (status != PostStatus.RECRUITING) {
            throw new DomainException("POST_CANNOT_CLOSE",
                    "모집 중인 모집글만 마감할 수 있습니다.");
        }
        if (currentMembers < 2) {
            throw new DomainException("POST_INSUFFICIENT_MEMBERS",
                    "파티를 결성하려면 최소 2명 이상이 필요합니다.",
                    Map.of("currentMembers", currentMembers, "requiredMinimum", 2));
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
            throw new DomainException("POST_HAS_PARTY_ROOM",
                    "파티룸이 생성된 모집글은 취소할 수 없습니다.");
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
            throw new DomainException("POST_NOT_EDITABLE",
                    "모집 중인 모집글만 수정할 수 있습니다.");
        }

        if (bossIds != null) {
            if (bossIds.isEmpty()) {
                throw new DomainException("POST_NO_BOSS_SELECTED",
                        "최소 1개 이상의 보스를 선택해야 합니다.");
            }
            this.bossIds = new ArrayList<>(bossIds);
        }

        if (requiredMembers != null) {
            if (requiredMembers < 2 || requiredMembers > 6) {
                throw new DomainException("POST_INVALID_MEMBER_COUNT",
                        "모집 인원은 2~6명이어야 합니다.");
            }
            if (requiredMembers < currentMembers) {
                throw new DomainException("POST_MEMBER_COUNT_BELOW_CURRENT",
                        "현재 파티원 수(" + currentMembers + "명)보다 적은 인원으로 변경할 수 없습니다.",
                        Map.of("currentMembers", currentMembers, "requestedMembers", requiredMembers));
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
                .orElseThrow(() -> new DomainException("APPLICATION_NOT_FOUND",
                        "해당 지원을 찾을 수 없습니다."));
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

    public WorldGroup getWorldGroup() {
        return worldGroup;
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
