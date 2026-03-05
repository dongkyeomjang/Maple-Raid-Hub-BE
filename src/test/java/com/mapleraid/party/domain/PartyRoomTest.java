package com.mapleraid.party.domain;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PartyRoomTest {

    private UserId leaderId;
    private CharacterId leaderCharacterId;
    private UserId member1Id;
    private CharacterId member1CharacterId;
    private UserId member2Id;
    private CharacterId member2CharacterId;
    private PartyRoom partyRoom;

    @BeforeEach
    void setUp() {
        leaderId = UserId.generate();
        leaderCharacterId = CharacterId.generate();
        member1Id = UserId.generate();
        member1CharacterId = CharacterId.generate();
        member2Id = UserId.generate();
        member2CharacterId = CharacterId.generate();

        partyRoom = PartyRoom.create(
                PostId.generate(),
                List.of("boss1"),
                leaderId,
                leaderCharacterId
        );
        partyRoom.addMember(member1Id, member1CharacterId);
        partyRoom.addMember(member2Id, member2CharacterId);
    }

    @Nested
    @DisplayName("removeMember - 파티 탈퇴")
    class RemoveMemberTest {

        @Test
        @DisplayName("일반 멤버가 탈퇴하면 활성 멤버에서 제외되고 탈퇴 멤버 목록에 포함된다")
        void memberLeaves_removedFromActiveAndAddedToLeft() {
            partyRoom.removeMember(member1Id);

            assertThat(partyRoom.getActiveMembers()).hasSize(2); // 리더 + member2
            assertThat(partyRoom.getActiveMemberIds()).doesNotContain(member1Id);
            assertThat(partyRoom.getLeftMembers()).hasSize(1);
            assertThat(partyRoom.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id);
            assertThat(partyRoom.getLeftMembers().get(0).getLeftAt()).isNotNull();
        }

        @Test
        @DisplayName("파티장은 탈퇴할 수 없다")
        void leaderCannotLeave() {
            assertThatThrownBy(() -> partyRoom.removeMember(leaderId))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_LEADER_CANNOT_LEAVE"));
        }

        @Test
        @DisplayName("파티에 속하지 않은 사용자는 탈퇴할 수 없다")
        void nonMemberCannotLeave() {
            UserId unknownId = UserId.generate();

            assertThatThrownBy(() -> partyRoom.removeMember(unknownId))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_MEMBER"));
        }

        @Test
        @DisplayName("멤버 탈퇴 시 확정된 일정이 해제된다")
        void memberLeaves_confirmedScheduleIsReset() {
            partyRoom.setSchedule(Instant.now().plusSeconds(3600));
            assertThat(partyRoom.isScheduleConfirmed()).isTrue();
            assertThat(partyRoom.getScheduledTime()).isNotNull();

            partyRoom.removeMember(member1Id);

            assertThat(partyRoom.isScheduleConfirmed()).isFalse();
            assertThat(partyRoom.getScheduledTime()).isNull();
        }

        @Test
        @DisplayName("일정이 확정되지 않은 상태에서 탈퇴해도 문제 없다")
        void memberLeaves_noSchedule_noError() {
            assertThat(partyRoom.isScheduleConfirmed()).isFalse();

            partyRoom.removeMember(member1Id);

            assertThat(partyRoom.isScheduleConfirmed()).isFalse();
            assertThat(partyRoom.getScheduledTime()).isNull();
        }

        @Test
        @DisplayName("이미 탈퇴한 멤버는 다시 탈퇴할 수 없다")
        void alreadyLeftMemberCannotLeaveAgain() {
            partyRoom.removeMember(member1Id);

            assertThatThrownBy(() -> partyRoom.removeMember(member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_MEMBER"));
        }

        @Test
        @DisplayName("탈퇴 후 getMemberCount는 활성 멤버만 센다")
        void afterLeave_memberCountReflectsOnlyActive() {
            assertThat(partyRoom.getMemberCount()).isEqualTo(3);

            partyRoom.removeMember(member1Id);

            assertThat(partyRoom.getMemberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("탈퇴 후 isMember는 false를 반환한다")
        void afterLeave_isMemberReturnsFalse() {
            assertThat(partyRoom.isMember(member1Id)).isTrue();

            partyRoom.removeMember(member1Id);

            assertThat(partyRoom.isMember(member1Id)).isFalse();
        }

        @Test
        @DisplayName("두 멤버가 모두 탈퇴해도 파티룸은 유지된다 (리더 1명 남음)")
        void allMembersLeave_partyRoomStillActive() {
            partyRoom.removeMember(member1Id);
            partyRoom.removeMember(member2Id);

            assertThat(partyRoom.getStatus()).isEqualTo(PartyRoomStatus.ACTIVE);
            assertThat(partyRoom.getMemberCount()).isEqualTo(1); // 리더만
            assertThat(partyRoom.getLeftMembers()).hasSize(2);
        }

        @Test
        @DisplayName("완료된 파티에서는 탈퇴할 수 없다")
        void cannotLeaveCompletedParty() {
            partyRoom.complete(leaderId);

            assertThatThrownBy(() -> partyRoom.removeMember(member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_ACTIVE"));
        }

        @Test
        @DisplayName("취소된 파티에서는 탈퇴할 수 없다")
        void cannotLeaveCanceledParty() {
            partyRoom.cancel(leaderId);

            assertThatThrownBy(() -> partyRoom.removeMember(member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_ACTIVE"));
        }
    }

    @Nested
    @DisplayName("kickMember - 파티원 추방")
    class KickMemberTest {

        @Test
        @DisplayName("파티장이 멤버를 추방하면 해당 멤버가 탈퇴 처리된다")
        void leaderKicksMember_memberIsRemoved() {
            partyRoom.kickMember(leaderId, member1Id);

            assertThat(partyRoom.getActiveMembers()).hasSize(2); // 리더 + member2
            assertThat(partyRoom.getActiveMemberIds()).doesNotContain(member1Id);
            assertThat(partyRoom.getLeftMembers()).hasSize(1);
            assertThat(partyRoom.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id);
        }

        @Test
        @DisplayName("파티장이 아닌 멤버는 추방할 수 없다")
        void nonLeaderCannotKick() {
            assertThatThrownBy(() -> partyRoom.kickMember(member1Id, member2Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_LEADER"));
        }

        @Test
        @DisplayName("파티장은 자기 자신을 추방할 수 없다")
        void leaderCannotKickSelf() {
            assertThatThrownBy(() -> partyRoom.kickMember(leaderId, leaderId))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_LEADER_CANNOT_LEAVE"));
        }

        @Test
        @DisplayName("추방 시 확정된 일정이 해제된다")
        void kickMember_confirmedScheduleIsReset() {
            partyRoom.setSchedule(Instant.now().plusSeconds(3600));
            assertThat(partyRoom.isScheduleConfirmed()).isTrue();

            partyRoom.kickMember(leaderId, member1Id);

            assertThat(partyRoom.isScheduleConfirmed()).isFalse();
            assertThat(partyRoom.getScheduledTime()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 멤버를 추방하려 하면 예외가 발생한다")
        void kickNonExistentMember_throwsException() {
            UserId unknownId = UserId.generate();

            assertThatThrownBy(() -> partyRoom.kickMember(leaderId, unknownId))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_MEMBER"));
        }

        @Test
        @DisplayName("이미 탈퇴한 멤버를 추방하려 하면 예외가 발생한다")
        void kickAlreadyLeftMember_throwsException() {
            partyRoom.removeMember(member1Id);

            assertThatThrownBy(() -> partyRoom.kickMember(leaderId, member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_MEMBER"));
        }

        @Test
        @DisplayName("완료된 파티에서는 추방할 수 없다")
        void cannotKickFromCompletedParty() {
            partyRoom.complete(leaderId);

            assertThatThrownBy(() -> partyRoom.kickMember(leaderId, member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_ACTIVE"));
        }

        @Test
        @DisplayName("취소된 파티에서는 추방할 수 없다")
        void cannotKickFromCanceledParty() {
            partyRoom.cancel(leaderId);

            assertThatThrownBy(() -> partyRoom.kickMember(leaderId, member1Id))
                    .isInstanceOf(CommonException.class)
                    .satisfies(ex -> assertThat(((CommonException) ex).getErrorCode().name())
                            .isEqualTo("PARTY_NOT_ACTIVE"));
        }
    }

    @Nested
    @DisplayName("getLeftMembers - 탈퇴 멤버 조회")
    class GetLeftMembersTest {

        @Test
        @DisplayName("탈퇴한 멤버가 없으면 빈 리스트를 반환한다")
        void noLeftMembers_returnsEmptyList() {
            assertThat(partyRoom.getLeftMembers()).isEmpty();
        }

        @Test
        @DisplayName("탈퇴와 추방 멤버가 모두 포함된다")
        void leftAndKickedMembers_allIncluded() {
            partyRoom.removeMember(member1Id); // 자발적 탈퇴
            partyRoom.kickMember(leaderId, member2Id); // 추방

            List<PartyMember> leftMembers = partyRoom.getLeftMembers();
            assertThat(leftMembers).hasSize(2);
            assertThat(leftMembers)
                    .extracting(PartyMember::getUserId)
                    .containsExactlyInAnyOrder(member1Id, member2Id);
        }
    }

    @Nested
    @DisplayName("탈퇴/추방과 다른 기능 간의 상호작용")
    class InteractionTest {

        @Test
        @DisplayName("탈퇴한 멤버는 레디 체크 대상에서 제외된다")
        void leftMemberExcludedFromReadyCheck() {
            partyRoom.removeMember(member1Id);
            partyRoom.startReadyCheck(leaderId);

            // 활성 멤버만 레디 해야 allReady
            partyRoom.markReady(leaderId);
            partyRoom.markReady(member2Id);

            assertThat(partyRoom.isAllReady()).isTrue();
        }

        @Test
        @DisplayName("탈퇴한 멤버에게는 unreadCount가 증가하지 않는다")
        void leftMemberDoesNotGetUnreadCount() {
            partyRoom.removeMember(member1Id);

            partyRoom.incrementUnreadCountExcept(leaderId);

            // member2만 증가, member1은 탈퇴했으므로 변경 없음
            assertThat(partyRoom.getUnreadCount(member2Id)).isEqualTo(1);
            assertThat(partyRoom.getUnreadCount(member1Id)).isEqualTo(0);
        }

        @Test
        @DisplayName("탈퇴 후 다시 같은 유저를 추가할 수 있다")
        void afterLeave_canReAddSameUser() {
            partyRoom.removeMember(member1Id);

            CharacterId newCharId = CharacterId.generate();
            partyRoom.addMember(member1Id, newCharId);

            assertThat(partyRoom.isMember(member1Id)).isTrue();
            assertThat(partyRoom.getMemberCount()).isEqualTo(3); // 리더 + member2 + 재가입 member1
            // 탈퇴 기록도 남아있어야 함
            assertThat(partyRoom.getLeftMembers()).hasSize(1);
        }
    }
}
