package com.mapleraid.chat.adapter.in.web.dto.response;

import com.mapleraid.chat.application.port.in.output.result.ReadMyDmRoomsResult;

import java.util.List;

public record ReadMyDmRoomsResponseDto(
        List<DmRoomResponseDto> dmRooms
) {
    public static ReadMyDmRoomsResponseDto from(ReadMyDmRoomsResult result) {
        List<DmRoomResponseDto> dmRooms = result.getRooms().stream()
                .map(DmRoomResponseDto::from)
                .toList();
        return new ReadMyDmRoomsResponseDto(dmRooms);
    }
}
