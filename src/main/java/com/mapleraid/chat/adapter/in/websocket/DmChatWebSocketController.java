package com.mapleraid.chat.adapter.in.websocket;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.chat.adapter.in.websocket.dto.DmChatMessage;
import com.mapleraid.chat.application.port.in.input.command.SendDmMessageInput;
import com.mapleraid.chat.application.port.in.output.result.SendDmMessageResult;
import com.mapleraid.chat.application.port.in.usecase.SendDmMessageUseCase;
import com.mapleraid.chat.domain.DirectMessageRoomId;
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
public class DmChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SendDmMessageUseCase sendDmMessageUseCase;

    @MessageMapping("/dm/{dmRoomId}")
    public void sendDmMessage(
            @DestinationVariable String dmRoomId,
            @Payload SendDmMessageRequest request,
            Principal principal) {

        if (principal == null) {
            return;
        }

        try {
            UserId userId = UserId.of(principal.getName());
            DirectMessageRoomId roomId = DirectMessageRoomId.of(dmRoomId);

            CharacterId senderCharId = request.senderCharacterId() != null
                    ? CharacterId.of(request.senderCharacterId())
                    : null;

            SendDmMessageResult result = sendDmMessageUseCase.execute(
                    SendDmMessageInput.of(roomId, userId, senderCharId, request.content()));

            DmChatMessage message = DmChatMessage.text(
                    dmRoomId,
                    userId.getValue().toString(),
                    result.getSenderNickname(),
                    senderCharId != null ? senderCharId.getValue().toString() : null,
                    result.getSenderCharacterName(),
                    result.getSenderCharacterImageUrl(),
                    result.getContent()
            );

            messagingTemplate.convertAndSend("/topic/dm/" + dmRoomId, message);

            String displayName = result.getSenderCharacterName() != null
                    ? result.getSenderCharacterName()
                    : result.getSenderNickname();
            messagingTemplate.convertAndSendToUser(
                    result.getRecipientUserId(),
                    "/queue/notifications",
                    new DmNotification(dmRoomId, displayName, "새 메시지가 도착했습니다.")
            );
        } catch (Exception e) {
            log.error("[DM WebSocket] 메시지 처리 실패 roomId={}: {}", dmRoomId, e.getMessage(), e);
        }
    }

    public void broadcastDmSystemMessage(String dmRoomId, String content) {
        DmChatMessage message = DmChatMessage.system(dmRoomId, content);
        messagingTemplate.convertAndSend("/topic/dm/" + dmRoomId, message);
    }

    public record SendDmMessageRequest(String senderNickname, String content, String senderCharacterId) {
    }

    public record DmNotification(String roomId, String senderNickname, String message) {
    }
}
