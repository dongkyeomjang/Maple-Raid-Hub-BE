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
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SendDmMessageService implements SendDmMessageUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final DmMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public SendDmMessageService(DirectMessageRoomRepository roomRepository,
                                DmMessageRepository messageRepository,
                                UserRepository userRepository,
                                CharacterRepository characterRepository) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    @Override
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

        String recipientUserId = room.getOtherUser(input.getSenderId()).getValue().toString();

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

        return SendDmMessageResult.from(savedMessage, senderNickname, senderCharacterName, senderCharacterImageUrl, recipientUserId);
    }
}
