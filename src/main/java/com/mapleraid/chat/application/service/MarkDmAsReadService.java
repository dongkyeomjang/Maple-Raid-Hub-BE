package com.mapleraid.chat.application.service;

import com.mapleraid.chat.application.port.in.input.command.MarkDmAsReadInput;
import com.mapleraid.chat.application.port.in.usecase.MarkDmAsReadUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.application.port.out.DmMessageRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkDmAsReadService implements MarkDmAsReadUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final DmMessageRepository messageRepository;

    @Override
    @Transactional
    public void execute(MarkDmAsReadInput input) {
        DirectMessageRoom room = roomRepository.findById(input.getRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.DM_ROOM_NOT_FOUND));

        if (!room.isParticipant(input.getUserId())) {
            throw new CommonException(ErrorCode.DM_NOT_PARTICIPANT);
        }

        messageRepository.markDmAsRead(input.getRoomId(), input.getUserId());

        room.markAsRead(input.getUserId());
        roomRepository.save(room);
    }
}
