package com.mapleraid.party.adapter.out.persistence;

import com.mapleraid.chat.adapter.out.persistence.mongo.ChatMessageDocument;
import com.mapleraid.chat.application.port.out.ChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class PartyChatMessagePersistenceAdapter implements PartyChatMessageRepository {
    private final ChatMessageRepository mongoRepository;

    public PartyChatMessagePersistenceAdapter(ChatMessageRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public void savePartyChatMessage(String roomId, String senderId, String senderNickname,
                                     String content, String messageType) {
        ChatMessageDocument doc = ChatMessageDocument.forPartyChat(
                UUID.randomUUID().toString(), roomId, senderId, senderNickname, content, messageType);
        mongoRepository.save(doc);
    }

    @Override
    public PartyChatMessagesPage findPartyChatMessages(String roomId, int limit, Instant before) {
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
        List<ChatMessageDocument> resultDocs = hasMore ? docs.subList(0, limit) : docs;
        List<PartyChatMessageDto> messages = resultDocs.stream()
                .map(doc -> new PartyChatMessageDto(
                        doc.getId(), doc.getRoomId(), doc.getSenderId(), doc.getSenderNickname(),
                        doc.getContent(), doc.getMessageType(), doc.getCreatedAt()))
                .toList().reversed();
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
        if (lastMessage == null) return null;
        return new LastMessageInfo(lastMessage.getContent(), lastMessage.getCreatedAt());
    }
}
