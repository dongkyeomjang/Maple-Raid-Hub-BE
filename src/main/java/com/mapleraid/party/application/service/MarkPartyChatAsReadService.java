package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.MarkPartyChatAsReadInput;
import com.mapleraid.party.application.port.in.usecase.MarkPartyChatAsReadUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkPartyChatAsReadService implements MarkPartyChatAsReadUseCase {
    private final PartyRoomRepository partyRoomRepository;

    public MarkPartyChatAsReadService(PartyRoomRepository partyRoomRepository) {
        this.partyRoomRepository = partyRoomRepository;
    }

    @Override
    public void execute(MarkPartyChatAsReadInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isMember(input.getUserId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }
        partyRoom.clearUnreadCount(input.getUserId());
        partyRoomRepository.save(partyRoom);
    }
}
