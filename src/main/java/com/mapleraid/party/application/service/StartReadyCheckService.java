package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.StartReadyCheckInput;
import com.mapleraid.party.application.port.in.output.result.StartReadyCheckResult;
import com.mapleraid.party.application.port.in.usecase.StartReadyCheckUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartReadyCheckService implements StartReadyCheckUseCase {
    private final PartyRoomRepository partyRoomRepository;

    @Override
    @Transactional
    public StartReadyCheckResult execute(StartReadyCheckInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        partyRoom.startReadyCheck(input.getRequesterId());
        PartyRoom saved = partyRoomRepository.save(partyRoom);
        return StartReadyCheckResult.from(saved);
    }
}
