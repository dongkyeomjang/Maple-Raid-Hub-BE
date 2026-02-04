package com.mapleraid.chat.adapter.out.persistence;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.adapter.out.persistence.mongo.ChatMessageDocument;
import com.mapleraid.chat.application.port.out.ChatMessageRepository;
import com.mapleraid.chat.application.port.out.DmMessageRepository;
import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.chat.domain.DirectMessageId;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class DmMessagePersistenceAdapter implements DmMessageRepository {

    private final ChatMessageRepository mongoRepository;

    public DmMessagePersistenceAdapter(ChatMessageRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public DirectMessage saveDmMessage(DirectMessage message) {
        ChatMessageDocument doc = ChatMessageDocument.forDm(
                message.getId().getValue().toString(),
                message.getRoomId().getValue().toString(),
                message.getSenderId() != null ? message.getSenderId().getValue().toString() : null,
                message.getSenderCharacterId() != null ? message.getSenderCharacterId().getValue().toString() : null,
                null,
                message.getContent(),
                message.getType().name()
        );
        mongoRepository.save(doc);
        return message;
    }

    @Override
    public Optional<DirectMessage> findDmMessageById(DirectMessageId id) {
        return mongoRepository.findById(id.getValue().toString())
                .filter(doc -> "DM".equals(doc.getRoomType()))
                .map(this::toDmDomain);
    }

    @Override
    public Page<DirectMessage> findDmMessagesByRoomId(DirectMessageRoomId roomId, Pageable pageable) {
        Page<ChatMessageDocument> docPage = mongoRepository.findByRoomIdOrderByCreatedAtDesc(
                roomId.getValue().toString(), pageable);

        List<DirectMessage> messages = docPage.getContent().stream()
                .filter(doc -> "DM".equals(doc.getRoomType()))
                .map(this::toDmDomain)
                .toList();

        return new PageImpl<>(messages, pageable, docPage.getTotalElements());
    }

    @Override
    public List<DirectMessage> findDmMessagesByRoomIdAsc(DirectMessageRoomId roomId) {
        return mongoRepository.findByRoomIdOrderByCreatedAtAsc(roomId.getValue().toString())
                .stream()
                .filter(doc -> "DM".equals(doc.getRoomType()))
                .map(this::toDmDomain)
                .toList();
    }

    @Override
    public DmMessagesPage findDmMessagesByRoomIdWithCursor(DirectMessageRoomId roomId, int limit, Instant before) {
        int fetchSize = limit + 1;
        String roomIdStr = roomId.getValue().toString();

        Page<ChatMessageDocument> page;
        if (before != null) {
            page = mongoRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    roomIdStr, before, PageRequest.of(0, fetchSize));
        } else {
            page = mongoRepository.findByRoomIdOrderByCreatedAtDesc(roomIdStr, PageRequest.of(0, fetchSize));
        }

        List<ChatMessageDocument> docs = page.getContent();
        boolean hasMore = docs.size() > limit;

        List<ChatMessageDocument> resultDocs = hasMore ? docs.subList(0, limit) : docs;

        List<DirectMessage> messages = resultDocs.stream()
                .filter(doc -> "DM".equals(doc.getRoomType()))
                .map(this::toDmDomain)
                .toList()
                .reversed();

        String nextCursor = messages.isEmpty() ? null : messages.get(0).getCreatedAt().toString();

        return new DmMessagesPage(messages, hasMore, nextCursor);
    }

    @Override
    public int markDmAsRead(DirectMessageRoomId roomId, UserId userId) {
        return (int) mongoRepository.markAsReadByRoomIdAndNotSender(
                roomId.getValue().toString(),
                userId.getValue().toString()
        );
    }

    private DirectMessage toDmDomain(ChatMessageDocument doc) {
        return DirectMessage.reconstitute(
                DirectMessageId.of(doc.getId()),
                DirectMessageRoomId.of(doc.getRoomId()),
                doc.getSenderId() != null ? UserId.of(doc.getSenderId()) : null,
                doc.getSenderCharacterId() != null ? CharacterId.of(doc.getSenderCharacterId()) : null,
                doc.getContent(),
                DirectMessage.MessageType.valueOf(doc.getMessageType()),
                doc.isRead(),
                doc.getCreatedAt()
        );
    }
}
