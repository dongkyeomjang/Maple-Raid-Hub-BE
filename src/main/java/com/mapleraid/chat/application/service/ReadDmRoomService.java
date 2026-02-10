package com.mapleraid.chat.application.service;

import com.mapleraid.chat.application.port.in.input.query.ReadDmRoomInput;
import com.mapleraid.chat.application.port.in.output.result.ReadDmRoomResult;
import com.mapleraid.chat.application.port.in.usecase.ReadDmRoomUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadDmRoomService implements ReadDmRoomUseCase {

    private final DirectMessageRoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadDmRoomResult execute(ReadDmRoomInput input) {
        DirectMessageRoom room = roomRepository.findById(input.getRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.DM_ROOM_NOT_FOUND));

        if (!room.isParticipant(input.getRequesterId())) {
            throw new CommonException(ErrorCode.DM_NOT_PARTICIPANT);
        }

        return ReadDmRoomResult.from(room);
    }
}
