package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.PartyRoomJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.PartyRoomJpaRepository;
import com.mapleraid.application.port.out.PartyRoomRepository;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.party.PartyRoomStatus;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PartyRoomRepositoryAdapter implements PartyRoomRepository {

    private final PartyRoomJpaRepository jpaRepository;

    public PartyRoomRepositoryAdapter(PartyRoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PartyRoom save(PartyRoom partyRoom) {
        String id = partyRoom.getId().getValue().toString();

        // 기존 엔티티가 있으면 조회하여 업데이트 (orphanRemoval 정상 작동)
        return jpaRepository.findByIdWithMembers(id)
                .map(existingEntity -> {
                    existingEntity.updateFromDomain(partyRoom);
                    return jpaRepository.save(existingEntity).toDomain();
                })
                .orElseGet(() -> {
                    // 새 엔티티 생성
                    PartyRoomJpaEntity entity = PartyRoomJpaEntity.fromDomain(partyRoom);
                    return jpaRepository.save(entity).toDomain();
                });
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
