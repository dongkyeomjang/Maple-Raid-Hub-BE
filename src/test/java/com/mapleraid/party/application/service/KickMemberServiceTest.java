package com.mapleraid.party.application.service;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.KickMemberInput;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KickMemberServiceTest {

    @Mock
    private PartyRoomRepository partyRoomRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private KickMemberService service;

    private UserId leaderId;
    private UserId member1Id;
    private UserId member2Id;
    private PartyRoomId partyRoomId;
    private PartyRoom partyRoom;

    @BeforeEach
    void setUp() {
        leaderId = UserId.generate();
        member1Id = UserId.generate();
        member2Id = UserId.generate();

        partyRoom = PartyRoom.create(PostId.generate(), List.of("boss1"), leaderId, CharacterId.generate());
        partyRoom.addMember(member1Id, CharacterId.generate());
        partyRoom.addMember(member2Id, CharacterId.generate());
        partyRoomId = partyRoom.getId();
    }

    @Test
    @DisplayName("파티장이 멤버를 추방하면 성공한다")
    void kickSuccess() {
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));
        given(partyRoomRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.execute(KickMemberInput.of(partyRoomId, leaderId, member1Id));

        assertThat(partyRoom.isMember(member1Id)).isFalse();
        assertThat(partyRoom.getLeftMembers()).hasSize(1);
        verify(availabilityRepository).deleteByPartyRoomIdAndUserId(partyRoomId, member1Id);
        verify(partyRoomRepository).save(partyRoom);
    }

    @Test
    @DisplayName("추방 시 확정된 일정이 해제된다")
    void kick_scheduleBecameUnconfirmed() {
        partyRoom.setSchedule(Instant.now().plusSeconds(3600));
        assertThat(partyRoom.isScheduleConfirmed()).isTrue();

        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));
        given(partyRoomRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.execute(KickMemberInput.of(partyRoomId, leaderId, member1Id));

        assertThat(partyRoom.isScheduleConfirmed()).isFalse();
        assertThat(partyRoom.getScheduledTime()).isNull();
    }

    @Test
    @DisplayName("파티장이 아닌 멤버가 추방하려 하면 예외 발생")
    void nonLeaderKick_throwsException() {
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));

        assertThatThrownBy(() ->
                service.execute(KickMemberInput.of(partyRoomId, member1Id, member2Id))
        ).isInstanceOf(CommonException.class);

        verify(availabilityRepository, never()).deleteByPartyRoomIdAndUserId(any(), any());
        verify(partyRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("파티장이 자기 자신을 추방하려 하면 예외 발생")
    void leaderKickSelf_throwsException() {
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));

        assertThatThrownBy(() ->
                service.execute(KickMemberInput.of(partyRoomId, leaderId, leaderId))
        ).isInstanceOf(CommonException.class);

        verify(availabilityRepository, never()).deleteByPartyRoomIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 파티룸에 대해 추방하려 하면 예외 발생")
    void partyNotFound_throwsException() {
        PartyRoomId unknownId = PartyRoomId.generate();
        given(partyRoomRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(KickMemberInput.of(unknownId, leaderId, member1Id))
        ).isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                        .isEqualTo("PARTY_NOT_FOUND"));
    }

    @Test
    @DisplayName("존재하지 않는 멤버를 추방하려 하면 예외 발생")
    void targetNotMember_throwsException() {
        UserId unknownId = UserId.generate();
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));

        assertThatThrownBy(() ->
                service.execute(KickMemberInput.of(partyRoomId, leaderId, unknownId))
        ).isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                        .isEqualTo("PARTY_NOT_MEMBER"));
    }

    @Test
    @DisplayName("이미 탈퇴한 멤버를 추방하려 하면 예외 발생")
    void alreadyLeftMember_throwsException() {
        partyRoom.removeMember(member1Id);
        given(partyRoomRepository.findById(partyRoomId)).willReturn(Optional.of(partyRoom));

        assertThatThrownBy(() ->
                service.execute(KickMemberInput.of(partyRoomId, leaderId, member1Id))
        ).isInstanceOf(CommonException.class)
                .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                        .isEqualTo("PARTY_NOT_MEMBER"));
    }
}
