package com.mapleraid.chat.adapter.in.web.query;

import com.mapleraid.chat.adapter.in.web.dto.response.DmMessagesPageResponseDto;
import com.mapleraid.chat.adapter.in.web.dto.response.DmRoomResponseDto;
import com.mapleraid.chat.adapter.in.web.dto.response.ReadMyDmRoomsResponseDto;
import com.mapleraid.chat.application.port.in.input.query.ReadDmMessagesInput;
import com.mapleraid.chat.application.port.in.input.query.ReadDmRoomInput;
import com.mapleraid.chat.application.port.in.input.query.ReadMyDmRoomsInput;
import com.mapleraid.chat.application.port.in.output.result.ReadDmMessagesResult;
import com.mapleraid.chat.application.port.in.usecase.ReadDmMessagesUseCase;
import com.mapleraid.chat.application.port.in.usecase.ReadDmRoomUseCase;
import com.mapleraid.chat.application.port.in.usecase.ReadMyDmRoomsUseCase;
import com.mapleraid.chat.domain.DirectMessageRoomId;
import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmQueryController {

    private final ReadMyDmRoomsUseCase readMyDmRoomsUseCase;
    private final ReadDmRoomUseCase readDmRoomUseCase;
    private final ReadDmMessagesUseCase readDmMessagesUseCase;

    /**
     * 내 DM 방 목록 조회하기
     */
    @GetMapping("/rooms")
    public ResponseDto<ReadMyDmRoomsResponseDto> getMyRooms(@CurrentUser UserId userId) {

        return ResponseDto.ok(
                ReadMyDmRoomsResponseDto.from(
                        readMyDmRoomsUseCase.execute(
                                ReadMyDmRoomsInput.of(userId)
                        )
                )
        );
    }

    /**
     * DM 방 상세 조회하기
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseDto<DmRoomResponseDto> getRoom(
            @CurrentUser UserId userId,
            @PathVariable String roomId) {

        return ResponseDto.ok(
                DmRoomResponseDto.from(
                        readDmRoomUseCase.execute(
                                ReadDmRoomInput.of(
                                        DirectMessageRoomId.of(roomId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * DM 메시지 목록 조회하기
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseDto<DmMessagesPageResponseDto> getMessages(
            @CurrentUser UserId userId,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String before) {

        Instant beforeInstant = before != null ? Instant.parse(before) : null;
        ReadDmMessagesResult result = readDmMessagesUseCase.execute(
                ReadDmMessagesInput.of(DirectMessageRoomId.of(roomId), userId, limit, beforeInstant));
        return ResponseDto.ok(DmMessagesPageResponseDto.from(result));
    }
}
