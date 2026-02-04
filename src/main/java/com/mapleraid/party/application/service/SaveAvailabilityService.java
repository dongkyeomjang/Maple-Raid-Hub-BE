package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.SaveAvailabilityInput;
import com.mapleraid.party.application.port.in.output.result.SaveAvailabilityResult;
import com.mapleraid.party.application.port.in.usecase.SaveAvailabilityUseCase;
import com.mapleraid.party.application.port.out.AvailabilityRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.PartyRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaveAvailabilityService implements SaveAvailabilityUseCase {
    private final AvailabilityRepository availabilityRepository;
    private final PartyRoomRepository partyRoomRepository;

    public SaveAvailabilityService(AvailabilityRepository availabilityRepository,
                                   PartyRoomRepository partyRoomRepository) {
        this.availabilityRepository = availabilityRepository;
        this.partyRoomRepository = partyRoomRepository;
    }

    @Override
    public SaveAvailabilityResult execute(SaveAvailabilityInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isMember(input.getUserId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }
        Availability availability = availabilityRepository
                .findByPartyRoomIdAndUserId(input.getPartyRoomId(), input.getUserId())
                .map(existing -> {
                    existing.updateSlots(input.getSlots());
                    return existing;
                })
                .orElseGet(() -> Availability.create(input.getPartyRoomId(), input.getUserId(), input.getSlots()));
        Availability saved = availabilityRepository.save(availability);
        return SaveAvailabilityResult.from(saved);
    }
}
