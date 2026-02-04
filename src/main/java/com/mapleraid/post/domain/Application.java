package com.mapleraid.post.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.user.domain.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * 파티 지원 엔티티
 */
public class Application {

    private final ApplicationId id;
    private final PostId postId;
    private final UserId applicantId;
    private final CharacterId characterId;
    private String message;
    private ApplicationStatus status;
    private Instant appliedAt;
    private Instant respondedAt;

    private Application(ApplicationId id, PostId postId, UserId applicantId,
                        CharacterId characterId, String message) {
        this.id = Objects.requireNonNull(id);
        this.postId = Objects.requireNonNull(postId);
        this.applicantId = Objects.requireNonNull(applicantId);
        this.characterId = Objects.requireNonNull(characterId);
        this.message = message;
        this.status = ApplicationStatus.APPLIED;
        this.appliedAt = Instant.now();
    }

    public static Application create(PostId postId, UserId applicantId,
                                     CharacterId characterId, String message) {
        return new Application(ApplicationId.generate(), postId, applicantId,
                characterId, message);
    }

    public static Application reconstitute(
            ApplicationId id, PostId postId, UserId applicantId, CharacterId characterId,
            String message, ApplicationStatus status,
            Instant appliedAt, Instant respondedAt) {
        Application app = new Application(id, postId, applicantId, characterId, message);
        app.status = status;
        app.appliedAt = appliedAt;
        app.respondedAt = respondedAt;
        return app;
    }

    /**
     * 수락
     */
    public void accept() {
        validateStatusTransition(ApplicationStatus.ACCEPTED);
        this.status = ApplicationStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    /**
     * 거절
     */
    public void reject() {
        validateStatusTransition(ApplicationStatus.REJECTED);
        this.status = ApplicationStatus.REJECTED;
        this.respondedAt = Instant.now();
    }

    /**
     * 지원 취소 (지원자가 직접)
     */
    public void withdraw() {
        if (status != ApplicationStatus.APPLIED) {
            throw new CommonException(ErrorCode.APPLICATION_CANNOT_WITHDRAW);
        }
        this.status = ApplicationStatus.WITHDRAWN;
        this.respondedAt = Instant.now();
    }

    /**
     * 모집글 취소로 인한 자동 취소
     */
    public void cancelByPost() {
        if (status == ApplicationStatus.APPLIED) {
            this.status = ApplicationStatus.CANCELED;
            this.respondedAt = Instant.now();
        }
    }

    private void validateStatusTransition(ApplicationStatus newStatus) {
        if (status != ApplicationStatus.APPLIED) {
            throw new CommonException(ErrorCode.APPLICATION_INVALID_STATUS);
        }
    }

    public boolean isPending() {
        return status == ApplicationStatus.APPLIED;
    }

    public boolean isAccepted() {
        return status == ApplicationStatus.ACCEPTED;
    }

    // Getters
    public ApplicationId getId() {
        return id;
    }

    public PostId getPostId() {
        return postId;
    }

    public UserId getApplicantId() {
        return applicantId;
    }

    public CharacterId getCharacterId() {
        return characterId;
    }

    public String getMessage() {
        return message;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }
}
