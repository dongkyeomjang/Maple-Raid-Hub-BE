package com.mapleraid.party.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.party.application.port.in.input.query.ReadPartyRoomInput;
import com.mapleraid.party.application.port.in.output.result.ReadPartyRoomResult;
import com.mapleraid.party.application.port.out.PartyChatMessageRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReadPartyRoomServiceTest {

    @Mock
    private PartyRoomRepository partyRoomRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private PartyChatMessageRepository partyChatMessageRepository;

    @InjectMocks
    private ReadPartyRoomService service;

    private UserId leaderId;
    private CharacterId leaderCharId;
    private UserId member1Id;
    private CharacterId member1CharId;
    private UserId member2Id;
    private CharacterId member2CharId;
    private PartyRoom partyRoom;

    @BeforeEach
    void setUp() {
        leaderId = UserId.generate();
        leaderCharId = CharacterId.generate();
        member1Id = UserId.generate();
        member1CharId = CharacterId.generate();
        member2Id = UserId.generate();
        member2CharId = CharacterId.generate();

        partyRoom = PartyRoom.create(PostId.generate(), List.of("boss1"), leaderId, leaderCharId);
        partyRoom.addMember(member1Id, member1CharId);
        partyRoom.addMember(member2Id, member2CharId);
    }

    private Character createCharacter(CharacterId charId, String name, UserId ownerId) {
        return Character.reconstitute(
                charId, name, "스카니아", EWorldGroup.NORMAL,
                "아크메이지(불,독)", 260, null, "ocid123",
                50000L, null, ownerId, EVerificationStatus.VERIFIED_OWNER,
                LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("탈퇴한 멤버가 leftMembers에 포함된다")
    void leftMembersIncluded() {
        partyRoom.removeMember(member1Id);

        given(partyRoomRepository.findById(partyRoom.getId())).willReturn(Optional.of(partyRoom));
        given(characterRepository.findByIds(any())).willReturn(List.of(
                createCharacter(leaderCharId, "리더캐릭", leaderId),
                createCharacter(member1CharId, "탈퇴캐릭", member1Id),
                createCharacter(member2CharId, "멤버2캐릭", member2Id)
        ));
        given(partyChatMessageRepository.getLastMessageInfo(anyString())).willReturn(null);

        ReadPartyRoomResult result = service.execute(
                ReadPartyRoomInput.of(partyRoom.getId(), leaderId)
        );

        assertThat(result.getMembers()).hasSize(2); // 리더 + member2
        assertThat(result.getLeftMembers()).hasSize(1);
        assertThat(result.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id.getValue().toString());
        assertThat(result.getLeftMembers().get(0).getCharacterName()).isEqualTo("탈퇴캐릭");
        assertThat(result.getLeftMembers().get(0).getLeftAt()).isNotNull();
    }

    @Test
    @DisplayName("탈퇴한 멤버가 없으면 leftMembers가 빈 리스트이다")
    void noLeftMembers_emptyList() {
        given(partyRoomRepository.findById(partyRoom.getId())).willReturn(Optional.of(partyRoom));
        given(characterRepository.findByIds(any())).willReturn(List.of(
                createCharacter(leaderCharId, "리더캐릭", leaderId),
                createCharacter(member1CharId, "멤버1캐릭", member1Id),
                createCharacter(member2CharId, "멤버2캐릭", member2Id)
        ));
        given(partyChatMessageRepository.getLastMessageInfo(anyString())).willReturn(null);

        ReadPartyRoomResult result = service.execute(
                ReadPartyRoomInput.of(partyRoom.getId(), leaderId)
        );

        assertThat(result.getMembers()).hasSize(3);
        assertThat(result.getLeftMembers()).isEmpty();
    }

    @Test
    @DisplayName("새로 합류한 멤버에게는 이전에 탈퇴한 멤버가 보이지 않는다")
    void newMemberCannotSeeEarlierLeftMembers() throws InterruptedException {
        // member1이 먼저 탈퇴
        partyRoom.removeMember(member1Id);

        // 약간의 시간 차이를 두고 새 멤버 합류
        Thread.sleep(10);
        UserId newMemberId = UserId.generate();
        CharacterId newMemberCharId = CharacterId.generate();
        partyRoom.addMember(newMemberId, newMemberCharId);

        given(partyRoomRepository.findById(partyRoom.getId())).willReturn(Optional.of(partyRoom));
        given(characterRepository.findByIds(any())).willReturn(List.of(
                createCharacter(leaderCharId, "리더캐릭", leaderId),
                createCharacter(member1CharId, "탈퇴캐릭", member1Id),
                createCharacter(member2CharId, "멤버2캐릭", member2Id),
                createCharacter(newMemberCharId, "새멤버캐릭", newMemberId)
        ));
        given(partyChatMessageRepository.getLastMessageInfo(anyString())).willReturn(null);

        // 새 멤버 시점에서 조회 - member1은 새 멤버 합류 전에 탈퇴했으므로 안 보여야 함
        ReadPartyRoomResult result = service.execute(
                ReadPartyRoomInput.of(partyRoom.getId(), newMemberId)
        );

        assertThat(result.getLeftMembers()).isEmpty();
    }

    @Test
    @DisplayName("기존 멤버에게는 탈퇴한 멤버가 보인다")
    void existingMemberCanSeeLeftMembers() throws InterruptedException {
        // member1이 탈퇴
        partyRoom.removeMember(member1Id);

        Thread.sleep(10);
        UserId newMemberId = UserId.generate();
        CharacterId newMemberCharId = CharacterId.generate();
        partyRoom.addMember(newMemberId, newMemberCharId);

        given(partyRoomRepository.findById(partyRoom.getId())).willReturn(Optional.of(partyRoom));
        given(characterRepository.findByIds(any())).willReturn(List.of(
                createCharacter(leaderCharId, "리더캐릭", leaderId),
                createCharacter(member1CharId, "탈퇴캐릭", member1Id),
                createCharacter(member2CharId, "멤버2캐릭", member2Id),
                createCharacter(newMemberCharId, "새멤버캐릭", newMemberId)
        ));
        given(partyChatMessageRepository.getLastMessageInfo(anyString())).willReturn(null);

        // 리더(기존 멤버) 시점에서 조회 - member1 탈퇴 이후이므로 보여야 함
        ReadPartyRoomResult result = service.execute(
                ReadPartyRoomInput.of(partyRoom.getId(), leaderId)
        );

        assertThat(result.getLeftMembers()).hasSize(1);
        assertThat(result.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id.getValue().toString());
    }

    @Test
    @DisplayName("새 멤버 합류 후 다른 멤버가 탈퇴하면 새 멤버에게도 보인다")
    void leftAfterNewMemberJoins_visibleToNewMember() throws InterruptedException {
        // 새 멤버 합류
        UserId newMemberId = UserId.generate();
        CharacterId newMemberCharId = CharacterId.generate();
        partyRoom.addMember(newMemberId, newMemberCharId);

        // 새 멤버 합류 후에 member1 탈퇴
        Thread.sleep(10);
        partyRoom.removeMember(member1Id);

        given(partyRoomRepository.findById(partyRoom.getId())).willReturn(Optional.of(partyRoom));
        given(characterRepository.findByIds(any())).willReturn(List.of(
                createCharacter(leaderCharId, "리더캐릭", leaderId),
                createCharacter(member1CharId, "탈퇴캐릭", member1Id),
                createCharacter(member2CharId, "멤버2캐릭", member2Id),
                createCharacter(newMemberCharId, "새멤버캐릭", newMemberId)
        ));
        given(partyChatMessageRepository.getLastMessageInfo(anyString())).willReturn(null);

        // 새 멤버 시점에서 조회 - member1은 새 멤버 합류 후에 탈퇴했으므로 보여야 함
        ReadPartyRoomResult result = service.execute(
                ReadPartyRoomInput.of(partyRoom.getId(), newMemberId)
        );

        assertThat(result.getLeftMembers()).hasSize(1);
        assertThat(result.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id.getValue().toString());
    }
}
