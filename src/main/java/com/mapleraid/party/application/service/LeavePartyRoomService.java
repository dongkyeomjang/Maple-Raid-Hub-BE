package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.LeavePartyRoomInput;
import com.mapleraid.party.application.port.in.usecase.LeavePartyRoomUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeavePartyRoomService implements LeavePartyRoomUseCase {
    private final PartyRoomRepository partyRoomRepository;

    @Override
    @Transactional
    public void execute(LeavePartyRoomInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        partyRoom.removeMember(input.getRequesterId());
        partyRoomRepository.save(partyRoom);
    }
}
