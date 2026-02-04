package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.SendPartyChatMessageInput;
import com.mapleraid.party.application.port.in.output.result.SendPartyChatMessageResult;
import com.mapleraid.party.application.port.in.usecase.SendPartyChatMessageUseCase;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SendPartyChatMessageService implements SendPartyChatMessageUseCase {

    private final PartyRoomRepository partyRoomRepository;
    private final PartyChatMessageRepository chatMessageRepository;

    public SendPartyChatMessageService(PartyRoomRepository partyRoomRepository,
                                       PartyChatMessageRepository chatMessageRepository) {
        this.partyRoomRepository = partyRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public SendPartyChatMessageResult execute(SendPartyChatMessageInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));

        if (!partyRoom.isMember(input.getSenderId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }

        chatMessageRepository.savePartyChatMessage(
                input.getPartyRoomId().getValue().toString(),
                input.getSenderId().getValue().toString(),
                input.getSenderNickname(),
                input.getContent(),
                "CHAT"
        );

        partyRoom.incrementUnreadCountExcept(input.getSenderId());
        partyRoomRepository.save(partyRoom);

        List<UserId> otherMemberIds = partyRoom.getActiveMembers().stream()
                .map(m -> m.getUserId())
                .filter(id -> !id.equals(input.getSenderId()))
                .toList();

        return SendPartyChatMessageResult.of(
                input.getPartyRoomId().getValue().toString(),
                input.getSenderId().getValue().toString(),
                input.getSenderNickname(),
                input.getContent(),
                otherMemberIds
        );
    }
}
