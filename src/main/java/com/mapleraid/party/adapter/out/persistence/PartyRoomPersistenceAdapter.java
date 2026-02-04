package com.mapleraid.party.adapter.out.persistence;

import com.mapleraid.party.adapter.out.persistence.jpa.PartyRoomJpaEntity;
import com.mapleraid.party.adapter.out.persistence.jpa.PartyRoomJpaRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.party.domain.PartyRoomStatus;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PartyRoomPersistenceAdapter implements PartyRoomRepository {

    private final PartyRoomJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public PartyRoomPersistenceAdapter(PartyRoomJpaRepository jpaRepository,
                                       EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public PartyRoom save(PartyRoom partyRoom) {
        String id = partyRoom.getId().getValue().toString();

        // 기존 엔티티가 있으면 멤버를 정리하여 persistence context 충돌 방지
        jpaRepository.findByIdWithMembers(id).ifPresent(existing -> {
            existing.getMembers().clear();
            entityManager.flush();
        });

        PartyRoomJpaEntity entity = PartyRoomJpaEntity.fromDomain(partyRoom);
        return entityManager.merge(entity).toDomain();
    }

    @Override
    public Optional<PartyRoom> findById(PartyRoomId id) {
        return jpaRepository.findByIdWithMembers(id.getValue().toString())
                .map(PartyRoomJpaEntity::toDomain);
    }

    @Override
    public Optional<PartyRoom> findByPostId(PostId postId) {
        return jpaRepository.findByPostId(postId.getValue().toString())
                .map(PartyRoomJpaEntity::toDomain);
    }

    @Override
    public List<PartyRoom> findByMemberUserId(UserId userId) {
        return jpaRepository.findByMemberUserId(userId.getValue().toString()).stream()
                .map(PartyRoomJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<PartyRoom> findByMemberUserIdAndStatus(UserId userId, PartyRoomStatus status) {
        return jpaRepository.findByMemberUserIdAndStatus(userId.getValue().toString(), status).stream()
                .map(PartyRoomJpaEntity::toDomain)
                .toList();
    }
}
