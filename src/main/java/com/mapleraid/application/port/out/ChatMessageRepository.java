package com.mapleraid.application.port.out;

import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageId;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 채팅 메시지 저장소 (MongoDB)
 * - DM 메시지와 파티 채팅 메시지 모두 처리
 */
public interface ChatMessageRepository {

    // DM 메시지
    DirectMessage saveDmMessage(DirectMessage message);

    Optional<DirectMessage> findDmMessageById(DirectMessageId id);

    Page<DirectMessage> findDmMessagesByRoomId(DirectMessageRoomId roomId, Pageable pageable);

    List<DirectMessage> findDmMessagesByRoomIdAsc(DirectMessageRoomId roomId);

    DmMessagesPage findDmMessagesByRoomIdWithCursor(DirectMessageRoomId roomId, int limit, java.time.Instant before);

    int markDmAsRead(DirectMessageRoomId roomId, UserId userId);

    // 파티 채팅 메시지
    void savePartyChatMessage(String roomId, String senderId, String senderNickname,
                              String content, String messageType);

    PartyChatMessagesPage findPartyChatMessages(String roomId, int limit, java.time.Instant before);

    // 공통
    String getLastMessageContent(String roomId);

    // 파티룸의 마지막 메시지 정보 조회
    LastMessageInfo getLastMessageInfo(String roomId);

    // DM 메시지 페이지네이션 응답
    record DmMessagesPage(
            List<DirectMessage> messages,
            boolean hasMore,
            String nextCursor
    ) {
    }

    // 마지막 메시지 정보
    record LastMessageInfo(String content, java.time.Instant timestamp) {
    }

    // 파티 채팅 메시지 DTO
    record PartyChatMessageDto(
            String id,
            String partyRoomId,
            String senderId,
            String senderNickname,
            String content,
            String type,
            java.time.Instant timestamp
    ) {
    }

    // 페이지네이션 응답
    record PartyChatMessagesPage(
            List<PartyChatMessageDto> messages,
            boolean hasMore,
            String nextCursor
    ) {
    }
}
