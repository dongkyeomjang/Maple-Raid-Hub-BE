package com.mapleraid.chat.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.chat.application.port.in.input.command.CreateDmRoomInput;
import com.mapleraid.chat.application.port.in.output.result.CreateDmRoomResult;
import com.mapleraid.chat.application.port.in.usecase.CreateDmRoomUseCase;
import com.mapleraid.chat.application.port.out.DirectMessageRoomRepository;
import com.mapleraid.chat.domain.DirectMessageRoom;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateDmRoomService implements CreateDmRoomUseCase {

    private final DirectMessageRoomRepository roomRepository;
    private final CharacterRepository characterRepository;

    public CreateDmRoomService(DirectMessageRoomRepository roomRepository,
                               CharacterRepository characterRepository) {
        this.roomRepository = roomRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public CreateDmRoomResult execute(CreateDmRoomInput input) {
        boolean hasVerifiedCharacter = characterRepository.findByOwnerId(input.getRequesterId()).stream()
                .anyMatch(c -> c.getVerificationStatus() == EVerificationStatus.VERIFIED_OWNER);

        if (!hasVerifiedCharacter) {
            throw new CommonException(ErrorCode.DM_REQUIRES_VERIFIED_CHARACTER);
        }

        DirectMessageRoom room;
        if (input.getPostId() != null) {
            room = roomRepository.findByPostIdAndUsers(input.getPostId(), input.getRequesterId(), input.getTargetUserId())
                    .orElseGet(() -> {
                        DirectMessageRoom newRoom = DirectMessageRoom.create(
                                input.getPostId(), input.getTargetUserId(), input.getRequesterId(),
                                input.getTargetCharacterId(), input.getRequesterCharacterId());
                        return roomRepository.save(newRoom);
                    });
        } else {
            room = roomRepository.findByUsersWithoutPost(input.getRequesterId(), input.getTargetUserId())
                    .orElseGet(() -> {
                        DirectMessageRoom newRoom = DirectMessageRoom.create(
                                null, input.getTargetUserId(), input.getRequesterId(),
                                input.getTargetCharacterId(), input.getRequesterCharacterId());
                        return roomRepository.save(newRoom);
                    });
        }

        return CreateDmRoomResult.from(room);
    }
}
