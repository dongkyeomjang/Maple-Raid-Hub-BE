package com.mapleraid.party.integration;

import com.mapleraid.character.domain.CharacterId;
import com.mapleraid.party.adapter.out.persistence.AvailabilityPersistenceAdapter;
import com.mapleraid.party.adapter.out.persistence.PartyRoomPersistenceAdapter;
import com.mapleraid.party.adapter.out.persistence.jpa.AvailabilityJpaEntity;
import com.mapleraid.party.adapter.out.persistence.jpa.AvailabilityJpaRepository;
import com.mapleraid.party.adapter.out.persistence.jpa.PartyMemberJpaEntity;
import com.mapleraid.party.adapter.out.persistence.jpa.PartyRoomJpaEntity;
import com.mapleraid.party.adapter.out.persistence.jpa.PartyRoomJpaRepository;
import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.party.domain.PartyRoomStatus;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.user.domain.UserId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PartyRoomPersistenceAdapter.class, AvailabilityPersistenceAdapter.class})
class PartyLeaveKickIntegrationTest {

    @Autowired
    private PartyRoomPersistenceAdapter partyRoomAdapter;

    @Autowired
    private AvailabilityPersistenceAdapter availabilityAdapter;

    @Autowired
    private PartyRoomJpaRepository partyRoomJpaRepository;

    @Autowired
    private AvailabilityJpaRepository availabilityJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private UserId leaderId;
    private CharacterId leaderCharId;
    private UserId member1Id;
    private CharacterId member1CharId;
    private UserId member2Id;
    private CharacterId member2CharId;

    @BeforeEach
    void setUp() {
        leaderId = UserId.generate();
        leaderCharId = CharacterId.generate();
        member1Id = UserId.generate();
        member1CharId = CharacterId.generate();
        member2Id = UserId.generate();
        member2CharId = CharacterId.generate();
    }

    private PartyRoom createAndSavePartyRoom() {
        PartyRoom partyRoom = PartyRoom.create(PostId.generate(), List.of("boss1", "boss2"), leaderId, leaderCharId);
        partyRoom.addMember(member1Id, member1CharId);
        partyRoom.addMember(member2Id, member2CharId);
        PartyRoom saved = partyRoomAdapter.save(partyRoom);
        flushAndClear();
        return saved;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // =========================================================================
    // 파티 탈퇴 관련 데이터 정합성 테스트
    // =========================================================================
    @Nested
    @DisplayName("파티 탈퇴 - DB 데이터 정합성")
    class LeaveDataConsistencyTest {

        @Test
        @DisplayName("탈퇴한 멤버의 leftAt이 DB에 저장된다")
        void memberLeave_leftAtPersistedInDb() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // 탈퇴 수행
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // DB에서 직접 확인
            PartyRoomJpaEntity entity = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            List<PartyMemberJpaEntity> memberEntities = entity.getMembers();

            assertThat(memberEntities).hasSize(3); // 탈퇴 포함 모두 저장

            PartyMemberJpaEntity leftMember = memberEntities.stream()
                    .filter(m -> m.getUserId().equals(member1Id.getValue().toString()))
                    .findFirst().orElseThrow();
            assertThat(leftMember.getLeftAt()).isNotNull();

            // 나머지 멤버들은 leftAt이 null
            long activeCount = memberEntities.stream().filter(m -> m.getLeftAt() == null).count();
            assertThat(activeCount).isEqualTo(2);
        }

        @Test
        @DisplayName("탈퇴한 멤버는 도메인으로 변환 시 leftMembers에 포함된다")
        void memberLeave_domainConversion_leftMembersCorrect() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            PartyRoom reloaded = partyRoomAdapter.findById(roomId).orElseThrow();
            assertThat(reloaded.getActiveMembers()).hasSize(2);
            assertThat(reloaded.getLeftMembers()).hasSize(1);
            assertThat(reloaded.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id);
            assertThat(reloaded.getLeftMembers().get(0).getLeftAt()).isNotNull();
        }

        @Test
        @DisplayName("탈퇴 후 다시 저장-로드해도 데이터가 유지된다 (영속성 라운드트립)")
        void memberLeave_roundTrip_dataIntact() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // 1차 탈퇴
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // 2차 로드 + 저장 (변경 없이)
            PartyRoom loaded2 = partyRoomAdapter.findById(roomId).orElseThrow();
            partyRoomAdapter.save(loaded2);
            flushAndClear();

            // 3차 로드 - 데이터가 여전히 맞는지
            PartyRoom loaded3 = partyRoomAdapter.findById(roomId).orElseThrow();
            assertThat(loaded3.getActiveMembers()).hasSize(2);
            assertThat(loaded3.getLeftMembers()).hasSize(1);
            assertThat(loaded3.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id);
        }

        @Test
        @DisplayName("일정이 확정된 상태에서 탈퇴 시, 일정이 해제되고 DB에 반영된다")
        void memberLeave_scheduleReset_persistedInDb() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // 일정 확정
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.setSchedule(Instant.now().plusSeconds(3600));
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // 일정 확정 상태 확인
            PartyRoomJpaEntity beforeLeave = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            assertThat(beforeLeave.isScheduleConfirmed()).isTrue();
            assertThat(beforeLeave.getScheduledTime()).isNotNull();

            // 탈퇴
            PartyRoom loaded2 = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded2.removeMember(member1Id);
            partyRoomAdapter.save(loaded2);
            flushAndClear();

            // 일정 해제 확인
            PartyRoomJpaEntity afterLeave = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            assertThat(afterLeave.isScheduleConfirmed()).isFalse();
            assertThat(afterLeave.getScheduledTime()).isNull();
        }
    }

    // =========================================================================
    // 파티원 추방 관련 데이터 정합성 테스트
    // =========================================================================
    @Nested
    @DisplayName("파티원 추방 - DB 데이터 정합성")
    class KickDataConsistencyTest {

        @Test
        @DisplayName("추방된 멤버의 leftAt이 DB에 저장된다")
        void kickMember_leftAtPersistedInDb() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.kickMember(leaderId, member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            PartyRoomJpaEntity entity = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            PartyMemberJpaEntity kickedMember = entity.getMembers().stream()
                    .filter(m -> m.getUserId().equals(member1Id.getValue().toString()))
                    .findFirst().orElseThrow();

            assertThat(kickedMember.getLeftAt()).isNotNull();
        }

        @Test
        @DisplayName("추방과 자발적 탈퇴가 동시에 존재하는 경우 둘 다 leftMembers에 포함된다")
        void kickAndLeave_bothInLeftMembers() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);  // 자발적 탈퇴
            loaded.kickMember(leaderId, member2Id);  // 추방
            partyRoomAdapter.save(loaded);
            flushAndClear();

            PartyRoom reloaded = partyRoomAdapter.findById(roomId).orElseThrow();
            assertThat(reloaded.getActiveMembers()).hasSize(1); // 리더만
            assertThat(reloaded.getLeftMembers()).hasSize(2);
            assertThat(reloaded.getLeftMembers())
                    .extracting(PartyMember::getUserId)
                    .containsExactlyInAnyOrder(member1Id, member2Id);
        }
    }

    // =========================================================================
    // Availability 삭제 데이터 정합성 테스트
    // =========================================================================
    @Nested
    @DisplayName("Availability 삭제 - DB 데이터 정합성")
    class AvailabilityDeleteTest {

        @Test
        @DisplayName("탈퇴 시 해당 멤버의 availability가 DB에서 삭제된다")
        void memberLeave_availabilityDeletedFromDb() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // member1의 availability 저장
            Availability avail = Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(14, 0)),
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(14, 30))
            ));
            availabilityAdapter.save(avail);
            flushAndClear();

            // availability가 저장되었는지 확인
            assertThat(availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member1Id)).isPresent();

            // 탈퇴 + availability 삭제
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // availability가 삭제되었는지 확인
            assertThat(availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member1Id)).isEmpty();

            // DB에서 직접 확인
            List<AvailabilityJpaEntity> remaining = availabilityJpaRepository.findByPartyRoomId(roomId.getValue().toString());
            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("한 멤버의 availability 삭제가 다른 멤버의 availability에 영향을 주지 않는다")
        void deleteAvailability_doesNotAffectOtherMembers() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // member1과 member2 모두 availability 저장
            Availability avail1 = Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(14, 0))
            ));
            Availability avail2 = Availability.create(roomId, member2Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(15, 0)),
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(15, 30))
            ));
            availabilityAdapter.save(avail1);
            availabilityAdapter.save(avail2);
            flushAndClear();

            // member1의 availability만 삭제
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // member1 삭제됨
            assertThat(availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member1Id)).isEmpty();

            // member2 유지됨
            Optional<Availability> member2Avail = availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member2Id);
            assertThat(member2Avail).isPresent();
            assertThat(member2Avail.get().getSlots()).hasSize(2);
        }

        @Test
        @DisplayName("availability가 없는 멤버가 탈퇴해도 오류가 발생하지 않는다")
        void deleteAvailability_noAvailability_noError() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // availability 없이 삭제 시도 - 오류 없어야 함
            assertThatCode(() -> {
                availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
                flushAndClear();
            }).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // findByMemberUserId 쿼리 정합성 테스트
    // =========================================================================
    @Nested
    @DisplayName("파티룸 목록 조회 쿼리 - 탈퇴 멤버 필터링 정합성")
    class FindByMemberUserIdTest {

        @Test
        @DisplayName("탈퇴한 멤버는 findByMemberUserId 결과에서 제외된다")
        void leftMember_excludedFromList() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // member1 탈퇴
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // member1로 조회 - 나오면 안됨
            List<PartyRoom> member1Rooms = partyRoomAdapter.findByMemberUserId(member1Id);
            assertThat(member1Rooms).isEmpty();

            // member2로 조회 - 나와야 함
            List<PartyRoom> member2Rooms = partyRoomAdapter.findByMemberUserId(member2Id);
            assertThat(member2Rooms).hasSize(1);

            // 리더로 조회 - 나와야 함
            List<PartyRoom> leaderRooms = partyRoomAdapter.findByMemberUserId(leaderId);
            assertThat(leaderRooms).hasSize(1);
        }

        @Test
        @DisplayName("추방된 멤버는 findByMemberUserId 결과에서 제외된다")
        void kickedMember_excludedFromList() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.kickMember(leaderId, member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            List<PartyRoom> member1Rooms = partyRoomAdapter.findByMemberUserId(member1Id);
            assertThat(member1Rooms).isEmpty();
        }

        @Test
        @DisplayName("탈퇴한 멤버가 다른 파티에 속해있으면 해당 파티만 조회된다")
        void leftMember_otherParty_onlyActiveShown() {
            // 파티1 생성
            PartyRoom party1 = createAndSavePartyRoom();

            // 파티2 생성 (member1이 리더가 아닌 다른 파티)
            UserId leader2Id = UserId.generate();
            PartyRoom party2 = PartyRoom.create(PostId.generate(), List.of("boss3"), leader2Id, CharacterId.generate());
            party2.addMember(member1Id, member1CharId);
            partyRoomAdapter.save(party2);
            flushAndClear();

            // 파티1에서 member1 탈퇴
            PartyRoom loadedParty1 = partyRoomAdapter.findById(party1.getId()).orElseThrow();
            loadedParty1.removeMember(member1Id);
            partyRoomAdapter.save(loadedParty1);
            flushAndClear();

            // member1로 조회 - 파티2만 나와야 함
            List<PartyRoom> member1Rooms = partyRoomAdapter.findByMemberUserId(member1Id);
            assertThat(member1Rooms).hasSize(1);
            assertThat(member1Rooms.get(0).getId()).isEqualTo(party2.getId());
        }

        @Test
        @DisplayName("탈퇴한 멤버는 findByMemberUserIdAndStatus에서도 제외된다")
        void leftMember_excludedFromStatusQuery() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            List<PartyRoom> activeRooms = partyRoomAdapter.findByMemberUserIdAndStatus(member1Id, PartyRoomStatus.ACTIVE);
            assertThat(activeRooms).isEmpty();
        }
    }

    // =========================================================================
    // 채팅방 유지 관련 (1인 남아도 파티룸 존재)
    // =========================================================================
    @Nested
    @DisplayName("채팅방 유지 - 파티룸은 삭제되지 않는다")
    class ChatRoomPersistenceTest {

        @Test
        @DisplayName("모든 멤버가 탈퇴해도 파티룸은 삭제되지 않고 리더만 남는다")
        void allMembersLeave_partyRoomStillExists() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            loaded.removeMember(member2Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // 파티룸이 여전히 존재
            Optional<PartyRoom> reloaded = partyRoomAdapter.findById(roomId);
            assertThat(reloaded).isPresent();
            assertThat(reloaded.get().getStatus()).isEqualTo(PartyRoomStatus.ACTIVE);
            assertThat(reloaded.get().getMemberCount()).isEqualTo(1);
            assertThat(reloaded.get().getActiveMembers().get(0).getUserId()).isEqualTo(leaderId);
            assertThat(reloaded.get().getLeftMembers()).hasSize(2);
        }

        @Test
        @DisplayName("2명 파티에서 1명 탈퇴 후 리더가 파티룸 조회 가능")
        void twoMemberParty_oneLeaves_leaderCanStillView() {
            // 2명 파티 생성
            PartyRoom partyRoom = PartyRoom.create(PostId.generate(), List.of("boss1"), leaderId, leaderCharId);
            partyRoom.addMember(member1Id, member1CharId);
            partyRoomAdapter.save(partyRoom);
            flushAndClear();

            PartyRoomId roomId = partyRoom.getId();

            // member1 탈퇴
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // 리더로 목록 조회 - 파티룸이 여전히 나타남
            List<PartyRoom> leaderRooms = partyRoomAdapter.findByMemberUserId(leaderId);
            assertThat(leaderRooms).hasSize(1);

            // 탈퇴 멤버로 목록 조회 - 안 나타남
            List<PartyRoom> member1Rooms = partyRoomAdapter.findByMemberUserId(member1Id);
            assertThat(member1Rooms).isEmpty();
        }
    }

    // =========================================================================
    // 파티 멤버 수 관련 정합성
    // =========================================================================
    @Nested
    @DisplayName("멤버 카운트 정합성")
    class MemberCountConsistencyTest {

        @Test
        @DisplayName("DB에 저장된 전체 멤버 수와 active 멤버 수가 다르다 (소프트 삭제)")
        void totalVsActiveCount() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // JPA 엔티티 레벨에서 전체 멤버
            PartyRoomJpaEntity entity = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            assertThat(entity.getMembers()).hasSize(3); // 전체 (소프트 삭제 포함)

            // 도메인 레벨에서 활성 멤버
            PartyRoom domainRoom = entity.toDomain();
            assertThat(domainRoom.getMemberCount()).isEqualTo(2); // 활성만
            assertThat(domainRoom.getMembers()).hasSize(3); // 전체
            assertThat(domainRoom.getActiveMembers()).hasSize(2);
            assertThat(domainRoom.getLeftMembers()).hasSize(1);
        }

        @Test
        @DisplayName("여러 번 탈퇴-재가입 반복 후 정합성 유지")
        void multipleLeaveRejoin_consistencyMaintained() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // member1 탈퇴
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.removeMember(member1Id);
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // member1 재가입
            CharacterId newCharId = CharacterId.generate();
            PartyRoom loaded2 = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded2.addMember(member1Id, newCharId);
            partyRoomAdapter.save(loaded2);
            flushAndClear();

            // 검증
            PartyRoom loaded3 = partyRoomAdapter.findById(roomId).orElseThrow();
            assertThat(loaded3.getActiveMembers()).hasSize(3); // 리더 + member2 + 재가입 member1
            assertThat(loaded3.getLeftMembers()).hasSize(1);    // 최초 탈퇴 기록

            // DB 엔티티 레벨 - 4개 행 (원래3 + 탈퇴1은 leftAt 있음 + 재가입1)
            PartyRoomJpaEntity entity = partyRoomJpaRepository.findByIdWithMembers(roomId.getValue().toString()).orElseThrow();
            assertThat(entity.getMembers()).hasSize(4);

            long activeInDb = entity.getMembers().stream().filter(m -> m.getLeftAt() == null).count();
            long leftInDb = entity.getMembers().stream().filter(m -> m.getLeftAt() != null).count();
            assertThat(activeInDb).isEqualTo(3);
            assertThat(leftInDb).isEqualTo(1);
        }
    }

    // =========================================================================
    // 히트맵 관련 데이터 정합성
    // =========================================================================
    @Nested
    @DisplayName("히트맵 - 탈퇴 멤버 availability 삭제 후 정합성")
    class HeatmapConsistencyTest {

        @Test
        @DisplayName("탈퇴 멤버의 availability 삭제 후 히트맵 데이터에 반영된다")
        void afterDelete_heatmapExcludesLeftMember() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            LocalDate today = LocalDate.now();

            // 3명 모두 availability 등록
            availabilityAdapter.save(Availability.create(roomId, leaderId, List.of(
                    new Availability.TimeSlot(today, LocalTime.of(14, 0)),
                    new Availability.TimeSlot(today, LocalTime.of(14, 30))
            )));
            availabilityAdapter.save(Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(today, LocalTime.of(14, 0)),
                    new Availability.TimeSlot(today, LocalTime.of(15, 0))
            )));
            availabilityAdapter.save(Availability.create(roomId, member2Id, List.of(
                    new Availability.TimeSlot(today, LocalTime.of(14, 0))
            )));
            flushAndClear();

            // 전체 availability 조회 - 3명
            List<Availability> allBefore = availabilityAdapter.findByPartyRoomId(roomId);
            assertThat(allBefore).hasSize(3);

            // member1 탈퇴 → availability 삭제
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // 전체 availability 조회 - 2명
            List<Availability> allAfter = availabilityAdapter.findByPartyRoomId(roomId);
            assertThat(allAfter).hasSize(2);
            assertThat(allAfter)
                    .extracting(a -> a.getUserId())
                    .containsExactlyInAnyOrder(leaderId, member2Id);

            // 14:00 슬롯에 member1의 데이터가 없어야 함
            long count14 = allAfter.stream()
                    .filter(a -> a.isAvailableAt(today, LocalTime.of(14, 0)))
                    .count();
            assertThat(count14).isEqualTo(2); // 리더 + member2만

            // 15:00 슬롯 - member1만 등록했던 슬롯
            long count15 = allAfter.stream()
                    .filter(a -> a.isAvailableAt(today, LocalTime.of(15, 0)))
                    .count();
            assertThat(count15).isEqualTo(0); // member1 삭제됨
        }

        @Test
        @DisplayName("availability 삭제 시 slot 테이블의 데이터도 함께 삭제된다")
        void deleteAvailability_slotsAlsoDeleted() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            availabilityAdapter.save(Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(10, 0)),
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(10, 30)),
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(11, 0))
            )));
            flushAndClear();

            // 삭제 전 slot 데이터 존재 확인
            AvailabilityJpaEntity entity = availabilityJpaRepository
                    .findByPartyRoomIdAndUserId(roomId.getValue().toString(), member1Id.getValue().toString())
                    .orElseThrow();
            assertThat(entity.getSlots()).hasSize(3);

            // 삭제
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // availability 삭제됨
            assertThat(availabilityJpaRepository
                    .findByPartyRoomIdAndUserId(roomId.getValue().toString(), member1Id.getValue().toString()))
                    .isEmpty();

            // 전체 availability_slots도 빈 테이블 (다른 멤버 없으므로)
            assertThat(availabilityJpaRepository.findByPartyRoomId(roomId.getValue().toString())).isEmpty();
        }
    }

    // =========================================================================
    // 엣지 케이스 통합 시나리오
    // =========================================================================
    @Nested
    @DisplayName("복합 시나리오 - 전체 흐름 통합 검증")
    class FullFlowScenarioTest {

        @Test
        @DisplayName("시나리오: 일정확정 → 멤버탈퇴 → 일정해제 → availability삭제 → 파티룸유지")
        void fullScenario_scheduleConfirm_leave_scheduleReset() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // Step 1: availability 등록
            availabilityAdapter.save(Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(20, 0))
            )));
            flushAndClear();

            // Step 2: 일정 확정
            PartyRoom step2 = partyRoomAdapter.findById(roomId).orElseThrow();
            step2.setSchedule(Instant.now().plusSeconds(7200));
            partyRoomAdapter.save(step2);
            flushAndClear();

            // Step 3: member1 탈퇴
            PartyRoom step3 = partyRoomAdapter.findById(roomId).orElseThrow();
            step3.removeMember(member1Id);
            partyRoomAdapter.save(step3);
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // 최종 검증
            PartyRoom finalState = partyRoomAdapter.findById(roomId).orElseThrow();

            // 파티룸 유지
            assertThat(finalState.getStatus()).isEqualTo(PartyRoomStatus.ACTIVE);

            // 일정 해제
            assertThat(finalState.isScheduleConfirmed()).isFalse();
            assertThat(finalState.getScheduledTime()).isNull();

            // 멤버 상태
            assertThat(finalState.getActiveMembers()).hasSize(2); // 리더 + member2
            assertThat(finalState.getLeftMembers()).hasSize(1);
            assertThat(finalState.getLeftMembers().get(0).getUserId()).isEqualTo(member1Id);

            // availability 삭제됨
            assertThat(availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member1Id)).isEmpty();

            // 리더로 목록 조회 가능
            assertThat(partyRoomAdapter.findByMemberUserId(leaderId)).hasSize(1);

            // 탈퇴 멤버로 목록 조회 불가
            assertThat(partyRoomAdapter.findByMemberUserId(member1Id)).isEmpty();
        }

        @Test
        @DisplayName("시나리오: 추방 → availability삭제 → 일정해제 → 새멤버합류 → 데이터정합")
        void fullScenario_kick_newMemberJoin() {
            PartyRoom partyRoom = createAndSavePartyRoom();
            PartyRoomId roomId = partyRoom.getId();

            // member1 availability 등록
            availabilityAdapter.save(Availability.create(roomId, member1Id, List.of(
                    new Availability.TimeSlot(LocalDate.now(), LocalTime.of(18, 0))
            )));

            // 일정 확정
            PartyRoom loaded = partyRoomAdapter.findById(roomId).orElseThrow();
            loaded.setSchedule(Instant.now().plusSeconds(3600));
            partyRoomAdapter.save(loaded);
            flushAndClear();

            // member1 추방
            PartyRoom step1 = partyRoomAdapter.findById(roomId).orElseThrow();
            step1.kickMember(leaderId, member1Id);
            partyRoomAdapter.save(step1);
            availabilityAdapter.deleteByPartyRoomIdAndUserId(roomId, member1Id);
            flushAndClear();

            // 새 멤버 합류
            UserId newMemberId = UserId.generate();
            CharacterId newMemberCharId = CharacterId.generate();
            PartyRoom step2 = partyRoomAdapter.findById(roomId).orElseThrow();
            step2.addMember(newMemberId, newMemberCharId);
            partyRoomAdapter.save(step2);
            flushAndClear();

            // 최종 검증
            PartyRoom finalState = partyRoomAdapter.findById(roomId).orElseThrow();

            assertThat(finalState.getActiveMembers()).hasSize(3); // 리더 + member2 + 새멤버
            assertThat(finalState.getLeftMembers()).hasSize(1);
            assertThat(finalState.isScheduleConfirmed()).isFalse();
            assertThat(finalState.getScheduledTime()).isNull();

            // 새 멤버로 목록 조회 가능
            assertThat(partyRoomAdapter.findByMemberUserId(newMemberId)).hasSize(1);

            // 추방된 멤버로 조회 불가
            assertThat(partyRoomAdapter.findByMemberUserId(member1Id)).isEmpty();

            // availability 확인
            assertThat(availabilityAdapter.findByPartyRoomIdAndUserId(roomId, member1Id)).isEmpty();
        }
    }
}
