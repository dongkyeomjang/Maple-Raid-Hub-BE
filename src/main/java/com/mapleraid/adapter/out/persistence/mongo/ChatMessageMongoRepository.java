package com.mapleraid.adapter.out.persistence.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {

    /**
     * 방 ID로 메시지 조회 (최신순)
     */
    Page<ChatMessageDocument> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    /**
     * 방 ID로 메시지 조회 (오래된순)
     */
    List<ChatMessageDocument> findByRoomIdOrderByCreatedAtAsc(String roomId);

    /**
     * DM 방의 읽지 않은 메시지 수
     */
    long countByRoomIdAndSenderIdNotAndIsReadFalse(String roomId, String currentUserId);

    /**
     * DM 방의 메시지 읽음 처리
     */
    @Query("{'roomId': ?0, 'senderId': {$ne: ?1}, 'isRead': false}")
    @Update("{'$set': {'isRead': true}}")
    long markAsReadByRoomIdAndNotSender(String roomId, String userId);

    /**
     * 특정 방의 최신 메시지 1개
     */
    ChatMessageDocument findFirstByRoomIdOrderByCreatedAtDesc(String roomId);

    /**
     * 방 삭제 시 모든 메시지 삭제
     */
    void deleteByRoomId(String roomId);

    /**
     * 파티 채팅 메시지 조회 (roomType = PARTY, 시간순)
     */
    List<ChatMessageDocument> findByRoomIdAndRoomTypeOrderByCreatedAtAsc(String roomId, String roomType);

    /**
     * 파티 채팅 메시지 조회 (최근 N개, 최신순)
     */
    Page<ChatMessageDocument> findByRoomIdAndRoomTypeOrderByCreatedAtDesc(String roomId, String roomType, Pageable pageable);

    /**
     * 파티 채팅 메시지 조회 (커서 기반 - before 시간 이전 메시지)
     */
    Page<ChatMessageDocument> findByRoomIdAndRoomTypeAndCreatedAtBeforeOrderByCreatedAtDesc(
            String roomId, String roomType, java.time.Instant before, Pageable pageable);

    /**
     * DM 메시지 조회 (커서 기반 - before 시간 이전 메시지)
     */
    Page<ChatMessageDocument> findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String roomId, java.time.Instant before, Pageable pageable);
}
