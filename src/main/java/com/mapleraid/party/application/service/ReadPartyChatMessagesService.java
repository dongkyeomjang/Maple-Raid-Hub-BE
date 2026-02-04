package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.query.ReadPartyChatMessagesInput;
import com.mapleraid.party.application.port.in.output.result.ReadPartyChatMessagesResult;
import com.mapleraid.party.application.port.in.usecase.ReadPartyChatMessagesUseCase;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReadPartyChatMessagesService implements ReadPartyChatMessagesUseCase {
    private final PartyRoomRepository partyRoomRepository;
    private final PartyChatMessageRepository chatMessageRepository;

    public ReadPartyChatMessagesService(PartyRoomRepository partyRoomRepository,
                                        PartyChatMessageRepository chatMessageRepository) {
        this.partyRoomRepository = partyRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ReadPartyChatMessagesResult execute(ReadPartyChatMessagesInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isMember(input.getRequesterId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }
        String roomIdStr = input.getPartyRoomId().getValue().toString();
        PartyChatMessageRepository.PartyChatMessagesPage page =
                chatMessageRepository.findPartyChatMessages(roomIdStr, input.getLimit(), input.getBefore());
        List<ReadPartyChatMessagesResult.ChatMessageItem> items = page.messages().stream()
                .map(msg -> new ReadPartyChatMessagesResult.ChatMessageItem(
                        msg.id(), msg.partyRoomId(), msg.senderId(), msg.senderNickname(),
                        msg.content(), msg.type(), msg.timestamp()))
                .toList();
        return new ReadPartyChatMessagesResult(items, page.hasMore(), page.nextCursor());
    }
}
