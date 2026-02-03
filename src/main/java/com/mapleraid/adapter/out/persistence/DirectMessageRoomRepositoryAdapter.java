package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.DirectMessageRoomJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.DirectMessageRoomJpaRepository;
import com.mapleraid.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectMessageRoomRepositoryAdapter implements DirectMessageRoomRepository {

    private final DirectMessageRoomJpaRepository jpaRepository;

    public DirectMessageRoomRepositoryAdapter(DirectMessageRoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DirectMessageRoom save(DirectMessageRoom room) {
        DirectMessageRoomJpaEntity entity = DirectMessageRoomJpaEntity.fromDomain(room);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<DirectMessageRoom> findById(DirectMessageRoomId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(DirectMessageRoomJpaEntity::toDomain);
    }

    @Override
    public List<DirectMessageRoom> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.getValue().toString()).stream()
                .map(DirectMessageRoomJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<DirectMessageRoom> findByPostIdAndUsers(PostId postId, UserId user1Id, UserId user2Id) {
        return jpaRepository.findByPostIdAndUsers(
                postId.getValue().toString(),
                user1Id.getValue().toString(),
                user2Id.getValue().toString()
        ).map(DirectMessageRoomJpaEntity::toDomain);
    }

    @Override
    public Optional<DirectMessageRoom> findByUsersWithoutPost(UserId user1Id, UserId user2Id) {
        return jpaRepository.findByUsersWithoutPost(
                user1Id.getValue().toString(),
                user2Id.getValue().toString()
        ).map(DirectMessageRoomJpaEntity::toDomain);
    }
}
