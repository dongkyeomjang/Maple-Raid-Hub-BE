package com.mapleraid.adapter.out.persistence.entity;

import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationId;
import com.mapleraid.domain.post.ApplicationStatus;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "applications")
public class ApplicationJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostJpaEntity post;

    @Column(name = "applicant_id", nullable = false, length = 36)
    private String applicantId;

    @Column(name = "character_id", nullable = false, length = 36)
    private String characterId;

    @Column(name = "message", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    protected ApplicationJpaEntity() {
    }

    public static ApplicationJpaEntity fromDomain(Application application, PostJpaEntity postEntity) {
        ApplicationJpaEntity entity = new ApplicationJpaEntity();
        entity.id = application.getId().getValue().toString();
        entity.post = postEntity;
        entity.applicantId = application.getApplicantId().getValue().toString();
        entity.characterId = application.getCharacterId().getValue().toString();
        entity.message = application.getMessage();
        entity.status = application.getStatus();
        entity.appliedAt = application.getAppliedAt();
        entity.respondedAt = application.getRespondedAt();
        return entity;
    }

    public Application toDomain() {
        return Application.reconstitute(
                ApplicationId.of(id),
                PostId.of(post.getId()),
                UserId.of(applicantId),
                CharacterId.of(characterId),
                message,
                status,
                appliedAt,
                respondedAt
        );
    }

    public String getId() {
        return id;
    }
}
