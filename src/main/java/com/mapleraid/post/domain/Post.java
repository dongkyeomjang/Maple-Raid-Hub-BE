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
    private final UserId authorId; // nullable: 비회원 모집글이면 null
    private final CharacterId characterId; // nullable: 비회원 모집글이면 null
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
    // Guest-only fields
    private boolean guest;
    private String guestWorldName;
    private String guestCharacterName;
    private String guestCharacterImageUrl;
    private String contactLink;
    private String guestPasswordHash;
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private Instant closedAt;

    private Post(PostId id, UserId authorId, CharacterId characterId, EWorldGroup EWorldGroup) {
        this.id = Objects.requireNonNull(id);
        this.authorId = authorId;
        this.characterId = characterId;
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
        Objects.requireNonNull(authorId, "authorId");
        Objects.requireNonNull(characterId, "characterId");
        validateCommon(bossIds, requiredMembers);

        Post post = new Post(PostId.generate(), authorId, characterId, EWorldGroup);
        post.bossIds = new ArrayList<>(bossIds);
        post.requiredMembers = requiredMembers;
        post.preferredTime = preferredTime;
        post.description = description;
        return post;
    }

    public static Post createGuest(EWorldGroup EWorldGroup,
                                   String guestWorldName, String guestCharacterName,
                                   String guestCharacterImageUrl, String contactLink,
                                   String guestPasswordHash,
                                   List<String> bossIds, int requiredMembers,
                                   String preferredTime, String description) {
        if (guestWorldName == null || guestWorldName.isBlank()) {
            throw new CommonException(ErrorCode.INVALID_WORLD);
        }
        if (guestCharacterName == null || guestCharacterName.isBlank()) {
            throw new CommonException(ErrorCode.NOT_FOUND_CHARACTER);
        }
        if (contactLink == null || contactLink.isBlank()) {
            throw new CommonException(ErrorCode.POST_GUEST_REQUIRES_CONTACT);
        }
        if (guestPasswordHash == null || guestPasswordHash.isBlank()) {
            throw new CommonException(ErrorCode.POST_GUEST_REQUIRES_PASSWORD);
        }
        validateCommon(bossIds, requiredMembers);

        Post post = new Post(PostId.generate(), null, null, EWorldGroup);
        post.guest = true;
        post.guestWorldName = guestWorldName;
        post.guestCharacterName = guestCharacterName;
        post.guestCharacterImageUrl = guestCharacterImageUrl;
        post.contactLink = contactLink;
        post.guestPasswordHash = guestPasswordHash;
        post.bossIds = new ArrayList<>(bossIds);
        post.requiredMembers = requiredMembers;
        post.preferredTime = preferredTime;
        post.description = description;
        return post;
    }

    private static void validateCommon(List<String> bossIds, int requiredMembers) {
        if (bossIds == null || bossIds.isEmpty()) {
            throw new CommonException(ErrorCode.POST_NO_BOSS_SELECTED);
        }
        if (requiredMembers < 2 || requiredMembers > 6) {
            throw new CommonException(ErrorCode.POST_INVALID_MEMBER_COUNT);
        }
    }

    public static Post reconstitute(
            PostId id, UserId authorId, CharacterId characterId, EWorldGroup EWorldGroup,
            List<String> bossIds, int requiredMembers, int currentMembers,
            String preferredTime, String description,
            PostStatus status, PartyRoomId partyRoomId,
            boolean guest, String guestWorldName, String guestCharacterName,
            String guestCharacterImageUrl, String contactLink, String guestPasswordHash,
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
        post.guest = guest;
        post.guestWorldName = guestWorldName;
        post.guestCharacterName = guestCharacterName;
        post.guestCharacterImageUrl = guestCharacterImageUrl;
        post.contactLink = contactLink;
        post.guestPasswordHash = guestPasswordHash;
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
        // 비회원 모집글에는 지원 불가 (정보 전달용)
        if (guest) {
            throw new CommonException(ErrorCode.POST_GUEST_CANNOT_APPLY);
        }

        // 본인 모집글에 지원 불가
        if (authorId != null && authorId.equals(applicantId)) {
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
     * 파티원 탈퇴/추방 처리: 현재 인원 감소 + 해당 지원 상태 변경
     */
    public void memberLeft(UserId userId) {
        if (currentMembers > 1) {
            currentMembers--;
        }
        applications.stream()
                .filter(app -> app.getApplicantId().equals(userId) && app.isAccepted())
                .findFirst()
                .ifPresent(Application::withdrawByLeave);
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
        this.status = PostStatus.CANCELED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();

        // 대기 중인 모든 지원 취소
        applications.stream()
                .filter(Application::isPending)
                .forEach(Application::cancelByPost);
    }

    /**
     * 파티 종료에 의한 모집글 취소
     * - 파티가 종료되면 더 이상 모집이 불필요하므로 모집글도 취소
     * - 이미 마감/취소/만료된 경우 무시
     */
    public void cancelByPartyCompletion() {
        if (status != PostStatus.RECRUITING) {
            return;
        }

        this.status = PostStatus.CANCELED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();

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
     * - 파티원이 있으면 (currentMembers >= 2) 자동 마감 (CLOSED)
     * - 작성자 혼자면 만료 (EXPIRED)
     */
    public boolean checkAndExpire() {
        if (status == PostStatus.RECRUITING && Instant.now().isAfter(expiresAt)) {
            if (currentMembers >= 2) {
                close();
            } else {
                this.status = PostStatus.EXPIRED;
                this.closedAt = Instant.now();
                this.updatedAt = Instant.now();
                applications.stream()
                        .filter(Application::isPending)
                        .forEach(Application::cancelByPost);
            }
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
        return authorId != null && userId != null && authorId.equals(userId);
    }

    public boolean isGuest() {
        return guest;
    }

    public String getGuestPasswordHash() {
        return guestPasswordHash;
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

    public String getGuestWorldName() {
        return guestWorldName;
    }

    public String getGuestCharacterName() {
        return guestCharacterName;
    }

    public String getGuestCharacterImageUrl() {
        return guestCharacterImageUrl;
    }

    public String getContactLink() {
        return contactLink;
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
