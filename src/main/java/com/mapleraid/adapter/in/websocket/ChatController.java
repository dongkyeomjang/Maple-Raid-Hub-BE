package com.mapleraid.adapter.in.websocket;

import com.mapleraid.adapter.in.websocket.dto.ChatMessage;
import com.mapleraid.adapter.in.websocket.dto.DmChatMessage;
import com.mapleraid.application.port.out.ChatMessageRepository;
import com.mapleraid.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.application.port.out.PartyRoomRepository;
import com.mapleraid.application.service.DirectMessageService;
import com.mapleraid.domain.character.CharacterId;
import com.mapleraid.domain.chat.DirectMessage;
import com.mapleraid.domain.chat.DirectMessageRoom;
import com.mapleraid.domain.chat.DirectMessageRoomId;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.user.UserId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PartyRoomRepository partyRoomRepository;
    private final DirectMessageRoomRepository dmRoomRepository;
    private final DirectMessageService dmService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          PartyRoomRepository partyRoomRepository,
                          DirectMessageRoomRepository dmRoomRepository,
                          DirectMessageService dmService,
                          ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.partyRoomRepository = partyRoomRepository;
        this.dmRoomRepository = dmRoomRepository;
        this.dmService = dmService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat/{partyRoomId}")
    public void sendMessage(
            @DestinationVariable String partyRoomId,
            @Payload SendMessageRequest request,
            Principal principal) {

        // Check authentication
        if (principal == null) {
            return; // Silently ignore unauthenticated messages
        }

        // Verify party membership
        UserId userId = UserId.of(principal.getName());
        PartyRoom partyRoom = partyRoomRepository.findById(PartyRoomId.of(partyRoomId))
                .orElse(null);

        if (partyRoom == null || !partyRoom.isMember(userId)) {
            return; // Silently ignore unauthorized messages
        }

        ChatMessage message = ChatMessage.chat(
                partyRoomId,
                userId.getValue().toString(),
                request.senderNickname(),
                request.content()
        );

        // MongoDB에 메시지 저장
        chatMessageRepository.savePartyChatMessage(
                partyRoomId,
                userId.getValue().toString(),
                request.senderNickname(),
                request.content(),
                "CHAT"
        );

        // 발신자 외 모든 멤버의 unreadCount 증가
        partyRoom.incrementUnreadCountExcept(userId);
        partyRoomRepository.save(partyRoom);

        messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, message);

        // 다른 멤버들에게 개인 알림 전송
        partyRoom.getActiveMembers().stream()
                .filter(m -> !m.getUserId().equals(userId))
                .forEach(m -> sendPartyChatNotification(
                        m.getUserId().getValue().toString(),
                        partyRoomId,
                        request.senderNickname()
                ));
    }

    private void sendPartyChatNotification(String targetUserId, String roomId, String senderNickname) {
        PartyChatNotification notification = new PartyChatNotification(roomId, senderNickname, "새 메시지가 도착했습니다.");
        messagingTemplate.convertAndSendToUser(targetUserId, "/queue/party-notifications", notification);
    }

    public void broadcastSystemMessage(String partyRoomId, String content) {
        ChatMessage message = ChatMessage.system(partyRoomId, content);
        messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, message);
    }

    public void broadcastEvent(String partyRoomId, String content, ChatMessage.MessageType type) {
        ChatMessage message = ChatMessage.event(partyRoomId, content, type);
        messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, message);
    }

    @MessageMapping("/dm/{dmRoomId}")
    public void sendDmMessage(
            @DestinationVariable String dmRoomId,
            @Payload SendDmMessageRequest request,
            Principal principal) {

        // Check authentication
        if (principal == null) {
            return; // Silently ignore unauthenticated messages
        }

        UserId userId = UserId.of(principal.getName());
        DirectMessageRoomId roomId = DirectMessageRoomId.of(dmRoomId);

        // Verify DM room participation
        DirectMessageRoom dmRoom = dmRoomRepository.findById(roomId).orElse(null);
        if (dmRoom == null || !dmRoom.isParticipant(userId)) {
            return; // Silently ignore unauthorized messages
        }

        // Save message via service
        CharacterId senderCharId = request.senderCharacterId() != null
                ? CharacterId.of(request.senderCharacterId())
                : null;
        DirectMessage savedMessage = dmService.sendMessage(roomId, userId, senderCharId, request.content());

        // 캐릭터 정보 조회
        var charInfo = dmService.getCharacterInfo(senderCharId);
        String characterName = charInfo != null ? charInfo.name() : null;
        String characterImageUrl = charInfo != null ? charInfo.imageUrl() : null;

        // Create response DTO
        DmChatMessage message = DmChatMessage.text(
                dmRoomId,
                userId.getValue().toString(),
                request.senderNickname(),
                senderCharId != null ? senderCharId.getValue().toString() : null,
                characterName,
                characterImageUrl,
                savedMessage.getContent()
        );

        // Broadcast to DM room topic
        messagingTemplate.convertAndSend("/topic/dm/" + dmRoomId, message);

        // Send notification to the other user
        UserId otherUserId = dmRoom.getOtherUser(userId);
        sendDmNotification(otherUserId.getValue().toString(), dmRoomId, characterName != null ? characterName : request.senderNickname());
    }

    // ==================== DM WebSocket ====================

    private void sendDmNotification(String targetUserId, String roomId, String senderNickname) {
        DmNotification notification = new DmNotification(roomId, senderNickname, "새 메시지가 도착했습니다.");
        messagingTemplate.convertAndSendToUser(targetUserId, "/queue/notifications", notification);
    }

    public void broadcastDmSystemMessage(String dmRoomId, String content) {
        DmChatMessage message = DmChatMessage.system(dmRoomId, content);
        messagingTemplate.convertAndSend("/topic/dm/" + dmRoomId, message);
    }

    public record SendMessageRequest(String senderNickname, String content) {
    }

    public record SendDmMessageRequest(String senderNickname, String content, String senderCharacterId) {
    }

    public record DmNotification(String roomId, String senderNickname, String message) {
    }

    public record PartyChatNotification(String roomId, String senderNickname, String message) {
    }
}
