package com.mapleraid.party.application.service;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.LeavePartyRoomInput;
import com.mapleraid.party.application.port.out.AvailabilityRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeavePartyRoomServiceTest {

    @Mock
    private PartyRoomRepository partyRoomRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LeavePartyRoomService service;

    private UserId leaderId;
    private UserId memberId;
    private PartyRoomId partyRoomId;
    private PartyRoom partyRoom;

    @BeforeEach
    void setUp() {
        leaderId = UserId.generate();
        memberId = UserId.generate();

        partyRoom = PartyRoom.create(PostId.generate(), List.of("boss1"), leaderId, CharacterId.generate());
        partyRoom.addMember(memberId, CharacterId.generate());
        partyRoomId = partyRoom.getId();
    }

    @Test
    @DisplayName("일반 멤버 탈퇴 - 성공 시 파티룸 저장 + availability 삭제")
    void leave_success() {
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));
        given(partyRoomRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.execute(LeavePartyRoomInput.of(partyRoomId, memberId));

        assertThat(partyRoom.isMember(memberId)).isFalse();
        assertThat(partyRoom.getLeftMembers()).hasSize(1);
        verify(partyRoomRepository).save(partyRoom);
        verify(availabilityRepository).deleteByPartyRoomIdAndUserId(partyRoomId, memberId);
    }

    @Test
    @DisplayName("파티장 탈퇴 시도 - 예외 발생")
    void leaderLeave_throwsException() {
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));

        assertThatThrownBy(() ->
                service.execute(LeavePartyRoomInput.of(partyRoomId, leaderId))
        ).isInstanceOf(CommonException.class);
    }

    @Test
    @DisplayName("존재하지 않는 파티룸 - 예외 발생")
    void partyNotFound_throwsException() {
        PartyRoomId unknownId = PartyRoomId.generate();
        given(partyRoomRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(LeavePartyRoomInput.of(unknownId, memberId))
        ).isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                        .isEqualTo("PARTY_NOT_FOUND"));
    }
}
