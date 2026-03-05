package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.SendPartyChatMessageInput;
import com.mapleraid.party.application.port.in.output.result.SendPartyChatMessageResult;
import com.mapleraid.party.application.port.in.usecase.SendPartyChatMessageUseCase;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.notification.application.event.PartyChatMessageReceivedEvent;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendPartyChatMessageService implements SendPartyChatMessageUseCase {

    private final PartyRoomRepository partyRoomRepository;
    private final PartyChatMessageRepository chatMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
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

        partyRoomRepository.incrementMemberUnreadCountExcept(input.getPartyRoomId(), input.getSenderId());

        List<UserId> otherMemberIds = partyRoom.getActiveMembers().stream()
                .map(PartyMember::getUserId)
                .filter(id -> !id.equals(input.getSenderId()))
                .toList();

        String messagePreview = input.getContent().length() > 50
                ? input.getContent().substring(0, 50) + "..."
                : input.getContent();

        String partyRoomIdStr = input.getPartyRoomId().getValue().toString();
        for (UserId recipientId : otherMemberIds) {
            eventPublisher.publishEvent(new PartyChatMessageReceivedEvent(
                    recipientId,
                    partyRoomIdStr,
                    input.getSenderNickname(),
                    messagePreview
            ));
        }

        return SendPartyChatMessageResult.of(
                input.getPartyRoomId().getValue().toString(),
                input.getSenderId().getValue().toString(),
                input.getSenderNickname(),
                input.getContent(),
                otherMemberIds
        );
    }
}
