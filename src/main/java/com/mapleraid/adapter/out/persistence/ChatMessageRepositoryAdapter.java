package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.mongo.ChatMessageDocument;
import com.mapleraid.adapter.out.persistence.mongo.ChatMessageMongoRepository;
import com.mapleraid.application.port.out.ChatMessageRepository;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageId;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final ChatMessageMongoRepository mongoRepository;

    public ChatMessageRepositoryAdapter(ChatMessageMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public DirectMessage saveDmMessage(DirectMessage message) {
        ChatMessageDocument doc = ChatMessageDocument.forDm(
                message.getId().getValue().toString(),
                message.getRoomId().getValue().toString(),
                message.getSenderId() != null ? message.getSenderId().getValue().toString() : null,
                message.getSenderCharacterId() != null ? message.getSenderCharacterId().getValue().toString() : null,
                null,  // senderNickname은 나중에 조회 시 채움
                message.getContent(),
                message.getType().name()
        );
        mongoRepository.save(doc);
        return message;
    }

    @Override
    public void savePartyChatMessage(String roomId, String senderId, String senderNickname,
                                     String content, String messageType) {
        ChatMessageDocument doc = ChatMessageDocument.forPartyChat(
                java.util.UUID.randomUUID().toString(),
                roomId,
                senderId,
                senderNickname,
                content,
                messageType
        );
        mongoRepository.save(doc);
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
    public DmMessagesPage findDmMessagesByRoomIdWithCursor(DirectMessageRoomId roomId, int limit, java.time.Instant before) {
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

        // 역순으로 반환 (오래된 것이 먼저 오도록)
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

    @Override
    public PartyChatMessagesPage findPartyChatMessages(String roomId, int limit, java.time.Instant before) {
        // limit + 1 개를 가져와서 hasMore 판단
        int fetchSize = limit + 1;

        Page<ChatMessageDocument> page;
        if (before != null) {
            page = mongoRepository.findByRoomIdAndRoomTypeAndCreatedAtBeforeOrderByCreatedAtDesc(
                    roomId, "PARTY", before, PageRequest.of(0, fetchSize));
        } else {
            page = mongoRepository.findByRoomIdAndRoomTypeOrderByCreatedAtDesc(
                    roomId, "PARTY", PageRequest.of(0, fetchSize));
        }

        List<ChatMessageDocument> docs = page.getContent();
        boolean hasMore = docs.size() > limit;

        // limit 개만 사용
        List<ChatMessageDocument> resultDocs = hasMore ? docs.subList(0, limit) : docs;

        // 역순으로 반환 (오래된 것이 먼저 오도록)
        List<PartyChatMessageDto> messages = resultDocs.stream()
                .map(doc -> new PartyChatMessageDto(
                        doc.getId(),
                        doc.getRoomId(),
                        doc.getSenderId(),
                        doc.getSenderNickname(),
                        doc.getContent(),
                        doc.getMessageType(),
                        doc.getCreatedAt()
                ))
                .toList()
                .reversed();

        // nextCursor는 가장 오래된 메시지의 timestamp
        String nextCursor = messages.isEmpty() ? null : messages.get(0).timestamp().toString();

        return new PartyChatMessagesPage(messages, hasMore, nextCursor);
    }

    @Override
    public String getLastMessageContent(String roomId) {
        ChatMessageDocument lastMessage = mongoRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        return lastMessage != null ? lastMessage.getContent() : null;
    }

    @Override
    public LastMessageInfo getLastMessageInfo(String roomId) {
        ChatMessageDocument lastMessage = mongoRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        if (lastMessage == null) {
            return null;
        }
        return new LastMessageInfo(lastMessage.getContent(), lastMessage.getCreatedAt());
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
