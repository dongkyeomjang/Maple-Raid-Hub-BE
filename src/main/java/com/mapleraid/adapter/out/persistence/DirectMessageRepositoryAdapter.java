package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.DirectMessageJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.DirectMessageJpaRepository;
import com.mapleraid.application.port.out.DirectMessageRepository;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageId;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectMessageRepositoryAdapter implements DirectMessageRepository {

    private final DirectMessageJpaRepository jpaRepository;

    public DirectMessageRepositoryAdapter(DirectMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DirectMessage save(DirectMessage message) {
        DirectMessageJpaEntity entity = DirectMessageJpaEntity.fromDomain(message);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<DirectMessage> findById(DirectMessageId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(DirectMessageJpaEntity::toDomain);
    }

    @Override
    public Page<DirectMessage> findByRoomId(DirectMessageRoomId roomId, Pageable pageable) {
        return jpaRepository.findByRoomId(roomId.getValue().toString(), pageable)
                .map(DirectMessageJpaEntity::toDomain);
    }

    @Override
    public List<DirectMessage> findByRoomIdOrderByCreatedAtAsc(DirectMessageRoomId roomId) {
        return jpaRepository.findByRoomIdOrderByCreatedAtAsc(roomId.getValue().toString()).stream()
                .map(DirectMessageJpaEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public int markAsReadByRoomIdAndNotSender(DirectMessageRoomId roomId, UserId userId) {
        return jpaRepository.markAsReadByRoomIdAndNotSender(
                roomId.getValue().toString(),
                userId.getValue().toString()
        );
    }
}
