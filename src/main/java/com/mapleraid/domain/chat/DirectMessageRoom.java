package com.mapleraid.domain.chat;

import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * 1:1 DM 방 Aggregate Root
 * - 게시글 관련 문의 용도로 모집자-지원자 간 1:1 채팅
 */
public class DirectMessageRoom {

    private final DirectMessageRoomId id;
    private final PostId postId;
    private final UserId user1Id;  // 보통 게시글 작성자
    private final UserId user2Id;  // 보통 문의자
    private CharacterId user1CharacterId;  // user1이 사용하는 캐릭터
    private CharacterId user2CharacterId;  // user2가 사용하는 캐릭터
    private int unreadCountUser1;
    private int unreadCountUser2;
    private DirectMessage lastMessage;
    private Instant lastMessageAt;
    private Instant createdAt;

    private DirectMessageRoom(DirectMessageRoomId id, PostId postId, UserId user1Id, UserId user2Id,
                              CharacterId user1CharacterId, CharacterId user2CharacterId) {
        this.id = Objects.requireNonNull(id);
        this.postId = postId; // nullable - 일반 DM일 수 있음
        this.user1Id = Objects.requireNonNull(user1Id);
        this.user2Id = Objects.requireNonNull(user2Id);
        this.user1CharacterId = user1CharacterId;
        this.user2CharacterId = user2CharacterId;
        this.unreadCountUser1 = 0;
        this.unreadCountUser2 = 0;
        this.createdAt = Instant.now();
    }

    public static DirectMessageRoom create(PostId postId, UserId user1Id, UserId user2Id,
                                           CharacterId user1CharacterId, CharacterId user2CharacterId) {
        if (user1Id.equals(user2Id)) {
            throw new DomainException("DM_SAME_USER", "자기 자신에게 DM을 보낼 수 없습니다.");
        }
        return new DirectMessageRoom(DirectMessageRoomId.generate(), postId, user1Id, user2Id,
                user1CharacterId, user2CharacterId);
    }

    public static DirectMessageRoom reconstitute(
            DirectMessageRoomId id, PostId postId, UserId user1Id, UserId user2Id,
            CharacterId user1CharacterId, CharacterId user2CharacterId,
            int unreadCountUser1, int unreadCountUser2,
            DirectMessage lastMessage, Instant lastMessageAt, Instant createdAt) {
        DirectMessageRoom room = new DirectMessageRoom(id, postId, user1Id, user2Id,
                user1CharacterId, user2CharacterId);
        room.unreadCountUser1 = unreadCountUser1;
        room.unreadCountUser2 = unreadCountUser2;
        room.lastMessage = lastMessage;
        room.lastMessageAt = lastMessageAt;
        room.createdAt = createdAt;
        return room;
    }

    public boolean isParticipant(UserId userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }

    public UserId getOtherUser(UserId userId) {
        if (user1Id.equals(userId)) return user2Id;
        if (user2Id.equals(userId)) return user1Id;
        throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
    }

    public void onNewMessage(DirectMessage message) {
        this.lastMessage = message;
        this.lastMessageAt = message.getCreatedAt();

        // 상대방 읽지 않음 카운트 증가
        if (message.getSenderId().equals(user1Id)) {
            this.unreadCountUser2++;
        } else {
            this.unreadCountUser1++;
        }
    }

    public void markAsRead(UserId userId) {
        if (user1Id.equals(userId)) {
            this.unreadCountUser1 = 0;
        } else if (user2Id.equals(userId)) {
            this.unreadCountUser2 = 0;
        }
    }

    public int getUnreadCount(UserId userId) {
        if (user1Id.equals(userId)) return unreadCountUser1;
        if (user2Id.equals(userId)) return unreadCountUser2;
        return 0;
    }

    public CharacterId getOtherUserCharacterId(UserId userId) {
        if (user1Id.equals(userId)) return user2CharacterId;
        if (user2Id.equals(userId)) return user1CharacterId;
        throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
    }

    public CharacterId getMyCharacterId(UserId userId) {
        if (user1Id.equals(userId)) return user1CharacterId;
        if (user2Id.equals(userId)) return user2CharacterId;
        throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
    }

    // Getters
    public DirectMessageRoomId getId() {
        return id;
    }

    public PostId getPostId() {
        return postId;
    }

    public UserId getUser1Id() {
        return user1Id;
    }

    public UserId getUser2Id() {
        return user2Id;
    }

    public CharacterId getUser1CharacterId() {
        return user1CharacterId;
    }

    public CharacterId getUser2CharacterId() {
        return user2CharacterId;
    }

    public int getUnreadCountUser1() {
        return unreadCountUser1;
    }

    public int getUnreadCountUser2() {
        return unreadCountUser2;
    }

    public DirectMessage getLastMessage() {
        return lastMessage;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
