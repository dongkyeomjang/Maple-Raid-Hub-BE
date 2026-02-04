package com.mapleraid.party.application.port.in.input.command;

import com.mapleraid.core.dto.SelfValidating;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ConfirmScheduleInput extends SelfValidating<ConfirmScheduleInput> {

    @NotNull(message = "파티룸 아이디는 필수입니다.")
    private final PartyRoomId partyRoomId;

    @NotNull(message = "요청자 아이디는 필수입니다.")
    private final UserId requesterId;

    @NotNull(message = "일정 시간은 필수입니다.")
    private final Instant scheduledTime;

    private ConfirmScheduleInput(PartyRoomId partyRoomId, UserId requesterId, Instant scheduledTime) {
        this.partyRoomId = partyRoomId;
        this.requesterId = requesterId;
        this.scheduledTime = scheduledTime;
        this.validateSelf();
    }

    public static ConfirmScheduleInput of(PartyRoomId partyRoomId, UserId requesterId, Instant scheduledTime) {
        return new ConfirmScheduleInput(partyRoomId, requesterId, scheduledTime);
    }
}
