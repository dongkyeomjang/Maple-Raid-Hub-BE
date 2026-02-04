package com.mapleraid.party.adapter.in.websocket;

import com.mapleraid.party.adapter.in.websocket.dto.ChatMessage;
import com.mapleraid.party.application.port.in.input.command.SendPartyChatMessageInput;
import com.mapleraid.party.application.port.in.output.result.SendPartyChatMessageResult;
import com.mapleraid.party.application.port.in.usecase.SendPartyChatMessageUseCase;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PartyChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SendPartyChatMessageUseCase sendPartyChatMessageUseCase;

    @MessageMapping("/chat/{partyRoomId}")
    public void sendMessage(@DestinationVariable String partyRoomId,
                            @Payload SendMessageRequest request, Principal principal) {
        if (principal == null) return;

        try {
            UserId userId = UserId.of(principal.getName());

            SendPartyChatMessageResult result = sendPartyChatMessageUseCase.execute(
                    SendPartyChatMessageInput.of(
                            PartyRoomId.of(partyRoomId), userId,
                            request.senderNickname(), request.content()));

            ChatMessage message = ChatMessage.chat(
                    partyRoomId, result.getSenderId(),
                    result.getSenderNickname(), result.getContent());
            messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, message);

            result.getOtherMemberIds().forEach(memberId ->
                    messagingTemplate.convertAndSendToUser(
                            memberId.getValue().toString(), "/queue/party-notifications",
                            new PartyChatNotification(partyRoomId, result.getSenderNickname(), "새 메시지가 도착했습니다.")));
        } catch (Exception e) {
            log.error("[Party WebSocket] 메시지 처리 실패 roomId={}: {}", partyRoomId, e.getMessage(), e);
        }
    }

    public void broadcastSystemMessage(String partyRoomId, String content) {
        messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, ChatMessage.system(partyRoomId, content));
    }

    public void broadcastEvent(String partyRoomId, String content, ChatMessage.MessageType type) {
        messagingTemplate.convertAndSend("/topic/party/" + partyRoomId, ChatMessage.event(partyRoomId, content, type));
    }

    public record SendMessageRequest(String senderNickname, String content) {
    }

    public record PartyChatNotification(String roomId, String senderNickname, String message) {
    }
}
