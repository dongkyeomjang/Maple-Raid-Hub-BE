package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.CompletePartyRoomInput;
import com.mapleraid.party.application.port.in.output.result.CompletePartyRoomResult;
import com.mapleraid.party.application.port.in.usecase.CompletePartyRoomUseCase;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompletePartyRoomService implements CompletePartyRoomUseCase {
    private final PartyRoomRepository partyRoomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CompletePartyRoomResult execute(CompletePartyRoomInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        partyRoom.complete(input.getRequesterId());
        for (UserId memberId : partyRoom.getActiveMemberIds()) {
            userRepository.findById(memberId).ifPresent(user -> {
                user.recordPartyCompletion();
                userRepository.save(user);
            });
        }
        PartyRoom saved = partyRoomRepository.save(partyRoom);
        return CompletePartyRoomResult.from(saved);
    }
}
