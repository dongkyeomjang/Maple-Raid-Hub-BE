package com.mapleraid.party.adapter.in.web.command;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.party.adapter.in.web.dto.request.AvailabilityRequestDto;
import com.mapleraid.party.adapter.in.web.dto.request.ConfirmScheduleRequestDto;
import com.mapleraid.party.adapter.in.web.dto.response.AvailabilityResponseDto;
import com.mapleraid.party.adapter.in.web.dto.response.PartyRoomResponseDto;
import com.mapleraid.party.application.port.in.input.command.CompletePartyRoomInput;
import com.mapleraid.party.application.port.in.input.command.ConfirmScheduleInput;
import com.mapleraid.party.application.port.in.input.command.KickMemberInput;
import com.mapleraid.party.application.port.in.input.command.LeavePartyRoomInput;
import com.mapleraid.party.application.port.in.input.command.MarkPartyChatAsReadInput;
import com.mapleraid.party.application.port.in.input.command.MarkReadyInput;
import com.mapleraid.party.application.port.in.input.command.SaveAvailabilityInput;
import com.mapleraid.party.application.port.in.input.command.StartReadyCheckInput;
import com.mapleraid.party.application.port.in.output.result.SaveAvailabilityResult;
import com.mapleraid.party.application.port.in.usecase.CompletePartyRoomUseCase;
import com.mapleraid.party.application.port.in.usecase.ConfirmScheduleUseCase;
import com.mapleraid.party.application.port.in.usecase.KickMemberUseCase;
import com.mapleraid.party.application.port.in.usecase.LeavePartyRoomUseCase;
import com.mapleraid.party.application.port.in.usecase.MarkPartyChatAsReadUseCase;
import com.mapleraid.party.application.port.in.usecase.MarkReadyUseCase;
import com.mapleraid.party.application.port.in.usecase.SaveAvailabilityUseCase;
import com.mapleraid.party.application.port.in.usecase.StartReadyCheckUseCase;
import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/party-rooms")
@RequiredArgsConstructor
public class PartyCommandController {

    private final LeavePartyRoomUseCase leavePartyRoomUseCase;
    private final KickMemberUseCase kickMemberUseCase;
    private final CompletePartyRoomUseCase completePartyRoomUseCase;
    private final StartReadyCheckUseCase startReadyCheckUseCase;
    private final MarkReadyUseCase markReadyUseCase;
    private final MarkPartyChatAsReadUseCase markPartyChatAsReadUseCase;
    private final SaveAvailabilityUseCase saveAvailabilityUseCase;
    private final ConfirmScheduleUseCase confirmScheduleUseCase;

    /**
     * 파티방 나가기
     */
    @PostMapping("/{partyRoomId}/leave")
    public ResponseDto<Void> leave(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        leavePartyRoomUseCase.execute(
                LeavePartyRoomInput.of(
                        PartyRoomId.of(partyRoomId),
                        userId
                )
        );
        return ResponseDto.ok(null);
    }

    /**
     * 파티원 추방 (파티장 전용)
     */
    @DeleteMapping("/{partyRoomId}/members/{targetUserId}")
    public ResponseDto<Void> kickMember(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @PathVariable String targetUserId) {

        kickMemberUseCase.execute(
                KickMemberInput.of(
                        PartyRoomId.of(partyRoomId),
                        userId,
                        UserId.of(targetUserId)
                )
        );
        return ResponseDto.ok(null);
    }

    /**
     * 파티방 완료하기
     */
    @PostMapping("/{partyRoomId}/complete")
    public ResponseDto<PartyRoomResponseDto> complete(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        return ResponseDto.ok(
                PartyRoomResponseDto.from(
                        completePartyRoomUseCase.execute(
                                CompletePartyRoomInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 레디 체크 시작하기
     */
    @PostMapping("/{partyRoomId}/ready-check")
    public ResponseDto<PartyRoomResponseDto> startReadyCheck(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        return ResponseDto.ok(
                PartyRoomResponseDto.from(
                        startReadyCheckUseCase.execute(
                                StartReadyCheckInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 레디 표시하기
     */
    @PostMapping("/{partyRoomId}/ready")
    public ResponseDto<PartyRoomResponseDto> markReady(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        return ResponseDto.ok(
                PartyRoomResponseDto.from(
                        markReadyUseCase.execute(
                                MarkReadyInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId
                                )
                        )
                )
        );
    }

    /**
     * 파티 채팅 읽음 처리하기
     */
    @PostMapping("/{partyRoomId}/read")
    public ResponseDto<Void> markChatAsRead(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId) {

        markPartyChatAsReadUseCase.execute(
                MarkPartyChatAsReadInput.of(
                        PartyRoomId.of(partyRoomId),
                        userId
                )
        );
        return ResponseDto.ok(null);
    }

    /**
     * 가능 시간 저장하기
     */
    @PutMapping("/{partyRoomId}/availability")
    public ResponseDto<AvailabilityResponseDto> saveAvailability(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @RequestBody AvailabilityRequestDto request) {

        List<Availability.TimeSlot> slots = request.slots().stream()
                .map(dto -> new Availability.TimeSlot(dto.date(), dto.time()))
                .toList();
        SaveAvailabilityResult result = saveAvailabilityUseCase.execute(
                SaveAvailabilityInput.of(PartyRoomId.of(partyRoomId), userId, slots));
        return ResponseDto.ok(AvailabilityResponseDto.from(result));
    }

    /**
     * 일정 확정하기
     */
    @PostMapping("/{partyRoomId}/schedule")
    public ResponseDto<PartyRoomResponseDto> confirmSchedule(
            @CurrentUser UserId userId,
            @PathVariable String partyRoomId,
            @RequestBody ConfirmScheduleRequestDto request) {

        return ResponseDto.ok(
                PartyRoomResponseDto.from(
                        confirmScheduleUseCase.execute(
                                ConfirmScheduleInput.of(
                                        PartyRoomId.of(partyRoomId),
                                        userId,
                                        request.scheduledTime()
                                )
                        )
                )
        );
    }
}
