package com.mapleraid.application.port.out;

import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.partyroom.Availability;
import com.mapleraid.domain.partyroom.AvailabilityId;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository {

    Availability save(Availability availability);

    Optional<Availability> findById(AvailabilityId id);

    List<Availability> findByPartyRoomId(PartyRoomId partyRoomId);

    Optional<Availability> findByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId);

    void deleteByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId);
}
