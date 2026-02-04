package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.MarkReadyInput;
import com.mapleraid.party.application.port.in.output.result.MarkReadyResult;
import com.mapleraid.party.application.port.in.usecase.MarkReadyUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkReadyService implements MarkReadyUseCase {
    private final PartyRoomRepository partyRoomRepository;

    public MarkReadyService(PartyRoomRepository partyRoomRepository) {
        this.partyRoomRepository = partyRoomRepository;
    }

    @Override
    public MarkReadyResult execute(MarkReadyInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        partyRoom.markReady(input.getRequesterId());
        PartyRoom saved = partyRoomRepository.save(partyRoom);
        return MarkReadyResult.from(saved);
    }
}
