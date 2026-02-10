package com.mapleraid.chat.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.chat.application.port.in.input.command.SendDmMessageInput;
import com.mapleraid.chat.application.port.in.output.result.SendDmMessageResult;
import com.mapleraid.chat.application.port.in.usecase.SendDmMessageUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.application.port.out.DmMessageRepository;
import com.mapleraid.chat.domain.DirectMessage;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.notification.application.event.DmMessageReceivedEvent;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendDmMessageService implements SendDmMessageUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final DmMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SendDmMessageResult execute(SendDmMessageInput input) {
        DirectMessageRoom room = roomRepository.findById(input.getRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.DM_ROOM_NOT_FOUND));

        if (!room.isParticipant(input.getSenderId())) {
            throw new CommonException(ErrorCode.DM_NOT_PARTICIPANT);
        }

        DirectMessage message = DirectMessage.createText(
                input.getRoomId(), input.getSenderId(), input.getSenderCharacterId(), input.getContent());
        DirectMessage savedMessage = messageRepository.saveDmMessage(message);

        room.onNewMessage(savedMessage);
        roomRepository.save(room);

        UserId recipientId = room.getOtherUser(input.getSenderId());
        String recipientUserId = recipientId.getValue().toString();

        // Enrich with sender info
        String senderNickname = null;
        String senderCharacterName = null;
        String senderCharacterImageUrl = null;

        User sender = userRepository.findById(input.getSenderId()).orElse(null);
        if (sender != null) {
            senderNickname = sender.getNickname();
        }

        if (input.getSenderCharacterId() != null) {
            Character character = characterRepository.findById(input.getSenderCharacterId()).orElse(null);
            if (character != null) {
                senderCharacterName = character.getCharacterName();
                senderCharacterImageUrl = character.getCharacterImageUrl();
            }
        }

        String preview = input.getContent().length() > 50
                ? input.getContent().substring(0, 50) + "..."
                : input.getContent();
        eventPublisher.publishEvent(new DmMessageReceivedEvent(
                recipientId,
                room.getId().getValue().toString(),
                senderNickname != null ? senderNickname : "알 수 없음",
                preview
        ));

        return SendDmMessageResult.from(savedMessage, senderNickname, senderCharacterName, senderCharacterImageUrl, recipientUserId);
    }
}
