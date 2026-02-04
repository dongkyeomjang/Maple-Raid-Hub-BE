package com.mapleraid.post.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.post.domain.PostStatus;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Column(name = "character_id", nullable = false, length = 36)
    private String characterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "world_group", nullable = false, length = 20)
    private EWorldGroup worldGroup;

    @ElementCollection
    @CollectionTable(name = "post_boss_ids", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "boss_id", length = 50)
    private List<String> bossIds = new ArrayList<>();

    @Column(name = "required_members")
    private int requiredMembers;

    @Column(name = "current_members")
    private int currentMembers;

    @Column(name = "preferred_time", length = 100)
    private String preferredTime;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "party_room_id", length = 36)
    private String partyRoomId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationJpaEntity> applications = new ArrayList<>();

    public static PostJpaEntity fromDomain(Post post) {
        PostJpaEntity entity = new PostJpaEntity();
        entity.id = post.getId().getValue().toString();
        entity.authorId = post.getAuthorId().getValue().toString();
        entity.characterId = post.getCharacterId().getValue().toString();
        entity.worldGroup = post.getWorldGroup();
        entity.bossIds = new ArrayList<>(post.getBossIds());
        entity.requiredMembers = post.getRequiredMembers();
        entity.currentMembers = post.getCurrentMembers();
        entity.preferredTime = post.getPreferredTime();
        entity.description = post.getDescription();
        entity.status = post.getStatus();
        entity.partyRoomId = post.getPartyRoomId() != null ? post.getPartyRoomId().getValue().toString() : null;
        entity.createdAt = post.getCreatedAt();
        entity.updatedAt = post.getUpdatedAt();
        entity.expiresAt = post.getExpiresAt();
        entity.closedAt = post.getClosedAt();

        // Convert applications
        entity.applications = post.getApplications().stream()
                .map(app -> ApplicationJpaEntity.fromDomain(app, entity))
                .toList();

        return entity;
    }

    public Post toDomain() {
        List<Application> domainApplications = applications.stream()
                .map(ApplicationJpaEntity::toDomain)
                .toList();

        return Post.reconstitute(
                PostId.of(id),
                UserId.of(authorId),
                CharacterId.of(characterId),
                worldGroup,
                new ArrayList<>(bossIds),
                requiredMembers,
                currentMembers,
                preferredTime,
                description,
                status,
                partyRoomId != null ? PartyRoomId.of(partyRoomId) : null,
                createdAt,
                updatedAt,
                expiresAt,
                closedAt,
                domainApplications
        );
    }
}
