package com.mapleraid.party.adapter.in.web.query;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.party.adapter.in.web.dto.response.AllAvailabilityResponseDto;
import com.mapleraid.party.adapter.in.web.dto.response.PartyChatMessagesPageResponseDto;
import com.mapleraid.party.adapter.in.web.dto.response.PartyRoomResponseDto;
import com.mapleraid.party.adapter.in.web.dto.response.ReadMyPartyRoomsResponseDto;
import com.mapleraid.party.application.port.in.input.query.ReadAllAvailabilityInput;
import com.mapleraid.party.application.port.in.input.query.ReadMyPartyRoomsInput;
import com.mapleraid.party.application.port.in.input.query.ReadPartyChatMessagesInput;
import com.mapleraid.party.application.port.in.input.query.ReadPartyRoomInput;
import com.mapleraid.party.application.port.in.usecase.ReadAllAvailabilityUseCase;
import com.mapleraid.party.application.port.in.usecase.ReadMyPartyRoomsUseCase;
import com.mapleraid.party.application.port.in.usecase.ReadPartyChatMessagesUseCase;
import com.mapleraid.party.application.port.in.usecase.ReadPartyRoomUseCase;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.party.domain.PartyRoomStatus;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/party-rooms")
@RequiredArgsConstructor
public class PartyQueryController {

    private final ReadMyPartyRoomsUseCase readMyPartyRoomsUseCase;
    private final ReadPartyRoomUseCase readPartyRoomUseCase;
    private final ReadPartyChatMessagesUseCase readPartyChatMessagesUseCase;
    private final ReadAllAvailabilityUseCase readAllAvailabilityUseCase;

    /**
     * 내 파티방 목록 조회하기
     */
    @GetMapping
    public ResponseDto<ReadMyPartyRoomsResponseDto> getMyPartyRooms(
            @CurrentUser UserId userId,
            @RequestParam(required = false) PartyRoomStatus status) {

        return ResponseDto.ok(
                ReadMyPartyRoomsResponseDto.from(
                        readMyPartyRoomsUseCase.execute(
                                ReadMyPartyRoomsInput.of(userId, status)
                        )
                )
        );
    }

    /**
     * 파티방 상세 조회하기
     */
    @GetMapping("/{partyRoomId}")
    public ResponseDto<PartyRoomResponseDto> getPartyRoom(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        return ResponseDto.ok(
                PartyRoomResponseDto.from(
                        readPartyRoomUseCase.execute(
                                ReadPartyRoomInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 파티 채팅 메시지 목록 조회하기
     */
    @GetMapping("/{partyRoomId}/messages")
    public ResponseDto<PartyChatMessagesPageResponseDto> getMessages(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String before) {

        Instant beforeInstant = before != null ? Instant.parse(before) : null;
        return ResponseDto.ok(
                PartyChatMessagesPageResponseDto.from(
                        readPartyChatMessagesUseCase.execute(
                                ReadPartyChatMessagesInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId,
                                        limit,
                                        beforeInstant
                                )
                        )
                )
        );
    }

    /**
     * 전체 가능 시간 조회하기
     */
    @GetMapping("/{partyRoomId}/availability")
    public ResponseDto<AllAvailabilityResponseDto> getAllAvailability(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        return ResponseDto.ok(
                AllAvailabilityResponseDto.from(
                        readAllAvailabilityUseCase.execute(
                                ReadAllAvailabilityInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId
                                )
                        )
                )
        );
    }
}
