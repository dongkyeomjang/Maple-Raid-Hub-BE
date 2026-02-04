package com.mapleraid.party.application.port.out;

import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.AvailabilityId;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository {

    Availability save(Availability availability);

    Optional<Availability> findById(AvailabilityId id);

    List<Availability> findByPartyRoomId(PartyRoomId partyRoomId);

    Optional<Availability> findByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId);

    void deleteByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId);
}
