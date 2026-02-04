package com.mapleraid.party.adapter.in.web.dto.response;

import com.mapleraid.party.application.port.in.output.result.ReadMyPartyRoomsResult;

import java.util.List;

public record ReadMyPartyRoomsResponseDto(
        List<PartyRoomResponseDto> partyRooms
) {
    public static ReadMyPartyRoomsResponseDto from(ReadMyPartyRoomsResult result) {
        List<PartyRoomResponseDto> partyRooms = result.getRooms().stream()
                .map(PartyRoomResponseDto::from)
                .toList();
        return new ReadMyPartyRoomsResponseDto(partyRooms);
    }
}
