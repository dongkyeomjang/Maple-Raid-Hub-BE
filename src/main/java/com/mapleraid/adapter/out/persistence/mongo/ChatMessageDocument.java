package com.mapleraid.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * 파티 채팅 메시지 MongoDB 도큐먼트
 */
@Document(collection = "chat_messages")
@CompoundIndex(name = "room_created_idx", def = "{'roomId': 1, 'createdAt': -1}")
public class ChatMessageDocument {

    @Id
    private String id;

    @Indexed
    private String roomId;

    private String roomType;  // "PARTY" or "DM"

    private String senderId;

    private String senderCharacterId;

    private String senderNickname;

    private String content;

    private String messageType;  // CHAT, SYSTEM, JOIN, LEAVE, etc.

    private boolean isRead;

    @Indexed
    private Instant createdAt;

    public ChatMessageDocument() {
    }

    public ChatMessageDocument(String id, String roomId, String roomType,
                               String senderId, String senderCharacterId, String senderNickname,
                               String content, String messageType,
                               boolean isRead, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.roomType = roomType;
        this.senderId = senderId;
        this.senderCharacterId = senderCharacterId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.messageType = messageType;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Factory methods
    public static ChatMessageDocument forPartyChat(String id, String roomId,
                                                   String senderId, String senderNickname,
                                                   String content, String messageType) {
        return new ChatMessageDocument(
                id, roomId, "PARTY",
                senderId, null, senderNickname,
                content, messageType,
                true,  // 파티 채팅은 읽음 처리 불필요
                Instant.now()
        );
    }

    public static ChatMessageDocument forDm(String id, String roomId,
                                            String senderId, String senderCharacterId, String senderNickname,
                                            String content, String messageType) {
        return new ChatMessageDocument(
                id, roomId, "DM",
                senderId, senderCharacterId, senderNickname,
                content, messageType,
                false,
                Instant.now()
        );
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderCharacterId() {
        return senderCharacterId;
    }

    public void setSenderCharacterId(String senderCharacterId) {
        this.senderCharacterId = senderCharacterId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
