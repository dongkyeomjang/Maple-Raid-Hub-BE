package com.mapleraid.application.service;

import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.ChatMessageRepository;
import com.mapleraid.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.character.VerificationStatus;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DirectMessageService {

    private final DirectMessageRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public DirectMessageService(DirectMessageRoomRepository roomRepository,
                                ChatMessageRepository messageRepository,
                                UserRepository userRepository,
                                CharacterRepository characterRepository) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * DM 방 생성 또는 기존 방 반환
     */
    public DirectMessageRoom getOrCreateRoom(PostId postId, UserId requesterId, UserId targetUserId,
                                             com.mapleraid.domain.character.CharacterId requesterCharacterId,
                                             com.mapleraid.domain.character.CharacterId targetCharacterId) {
        // 인증된 캐릭터를 소유하고 있는지 확인
        boolean hasVerifiedCharacter = characterRepository.findByOwnerId(requesterId).stream()
                .anyMatch(c -> c.getVerificationStatus() == VerificationStatus.VERIFIED_OWNER);

        if (!hasVerifiedCharacter) {
            throw new DomainException("DM_REQUIRES_VERIFIED_CHARACTER",
                    "DM을 보내려면 인증된 캐릭터가 필요합니다.");
        }

        // 동일한 게시글에 대해 두 사용자 간 기존 방이 있는지 확인
        if (postId != null) {
            return roomRepository.findByPostIdAndUsers(postId, requesterId, targetUserId)
                    .orElseGet(() -> {
                        // requester는 user2 (문의자), target은 user1 (게시글 작성자)
                        DirectMessageRoom newRoom = DirectMessageRoom.create(
                                postId, targetUserId, requesterId, targetCharacterId, requesterCharacterId);
                        return roomRepository.save(newRoom);
                    });
        }

        // 게시글 없는 일반 DM
        return roomRepository.findByUsersWithoutPost(requesterId, targetUserId)
                .orElseGet(() -> {
                    DirectMessageRoom newRoom = DirectMessageRoom.create(
                            null, targetUserId, requesterId, targetCharacterId, requesterCharacterId);
                    return roomRepository.save(newRoom);
                });
    }

    /**
     * 내 DM 방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DirectMessageRoom> getMyRooms(UserId userId) {
        return roomRepository.findByUserId(userId);
    }

    /**
     * DM 방 상세 조회
     */
    @Transactional(readOnly = true)
    public DirectMessageRoom getRoom(DirectMessageRoomId roomId, UserId requesterId) {
        DirectMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DomainException("DM_ROOM_NOT_FOUND", "DM 방을 찾을 수 없습니다."));

        if (!room.isParticipant(requesterId)) {
            throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
        }

        return room;
    }

    /**
     * 메시지 전송
     */
    public DirectMessage sendMessage(DirectMessageRoomId roomId, UserId senderId,
                                     com.mapleraid.domain.character.CharacterId senderCharacterId, String content) {
        DirectMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DomainException("DM_ROOM_NOT_FOUND", "DM 방을 찾을 수 없습니다."));

        if (!room.isParticipant(senderId)) {
            throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
        }

        DirectMessage message = DirectMessage.createText(roomId, senderId, senderCharacterId, content);
        DirectMessage savedMessage = messageRepository.saveDmMessage(message);

        // 방의 lastMessage 업데이트
        room.onNewMessage(savedMessage);
        roomRepository.save(room);

        return savedMessage;
    }

    /**
     * 메시지 조회
     */
    @Transactional(readOnly = true)
    public Page<DirectMessage> getMessages(DirectMessageRoomId roomId, UserId requesterId, int page, int size) {
        DirectMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DomainException("DM_ROOM_NOT_FOUND", "DM 방을 찾을 수 없습니다."));

        if (!room.isParticipant(requesterId)) {
            throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findDmMessagesByRoomId(roomId, pageable);
    }

    /**
     * 메시지 조회 (커서 기반 페이지네이션)
     */
    @Transactional(readOnly = true)
    public ChatMessageRepository.DmMessagesPage getMessagesWithCursor(
            DirectMessageRoomId roomId, UserId requesterId, int limit, java.time.Instant before) {
        DirectMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DomainException("DM_ROOM_NOT_FOUND", "DM 방을 찾을 수 없습니다."));

        if (!room.isParticipant(requesterId)) {
            throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
        }

        return messageRepository.findDmMessagesByRoomIdWithCursor(roomId, limit, before);
    }

    /**
     * 읽음 처리
     */
    public void markAsRead(DirectMessageRoomId roomId, UserId userId) {
        DirectMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new DomainException("DM_ROOM_NOT_FOUND", "DM 방을 찾을 수 없습니다."));

        if (!room.isParticipant(userId)) {
            throw new DomainException("DM_NOT_PARTICIPANT", "해당 DM 방의 참가자가 아닙니다.");
        }

        // 메시지 읽음 처리
        messageRepository.markDmAsRead(roomId, userId);

        // 방의 읽지 않음 카운트 초기화
        room.markAsRead(userId);
        roomRepository.save(room);
    }

    /**
     * 사용자 닉네임 조회
     */
    @Transactional(readOnly = true)
    public String getUserNickname(UserId userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("Unknown");
    }

    /**
     * 캐릭터 정보 조회
     */
    @Transactional(readOnly = true)
    public CharacterInfo getCharacterInfo(com.mapleraid.domain.character.CharacterId characterId) {
        if (characterId == null) {
            return null;
        }
        return characterRepository.findById(characterId)
                .map(c -> new CharacterInfo(c.getCharacterName(), c.getCharacterImageUrl()))
                .orElse(null);
    }

    public record CharacterInfo(String name, String imageUrl) {
    }
}
