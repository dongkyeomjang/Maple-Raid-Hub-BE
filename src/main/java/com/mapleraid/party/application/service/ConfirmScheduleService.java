package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.ConfirmScheduleInput;
import com.mapleraid.party.application.port.in.output.result.ConfirmScheduleResult;
import com.mapleraid.party.application.port.in.usecase.ConfirmScheduleUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmScheduleService implements ConfirmScheduleUseCase {
    private final PartyRoomRepository partyRoomRepository;

    @Override
    @Transactional
    public ConfirmScheduleResult execute(ConfirmScheduleInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isLeader(input.getRequesterId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_LEADER);
        }
        partyRoom.setSchedule(input.getScheduledTime());
        PartyRoom saved = partyRoomRepository.save(partyRoom);
        return ConfirmScheduleResult.from(saved);
    }
}
