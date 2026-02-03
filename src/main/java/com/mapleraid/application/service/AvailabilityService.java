package com.mapleraid.application.service;

import com.mapleraid.adapter.in.web.dto.party.AllAvailabilityResponse;
import com.mapleraid.adapter.in.web.dto.party.AvailabilityResponse;
import com.mapleraid.application.port.out.AvailabilityRepository;
import com.mapleraid.application.port.out.CharacterRepository;
import com.mapleraid.application.port.out.PartyRoomRepository;
import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.character.Character;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.party.PartyMember;
import com.mapleraid.domain.party.PartyRoom;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.partyroom.Availability;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final PartyRoomRepository partyRoomRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
                               PartyRoomRepository partyRoomRepository,
                               UserRepository userRepository,
                               CharacterRepository characterRepository) {
        this.availabilityRepository = availabilityRepository;
        this.partyRoomRepository = partyRoomRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * 가용시간 저장/업데이트
     */
    public Availability saveAvailability(PartyRoomId partyRoomId, UserId userId,
                                         List<Availability.TimeSlot> slots) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND", "파티룸을 찾을 수 없습니다."));

        if (!partyRoom.isMember(userId)) {
            throw new DomainException("PARTY_NOT_MEMBER", "해당 파티룸의 멤버가 아닙니다.");
        }

        // 기존 가용시간 조회 또는 새로 생성
        Availability availability = availabilityRepository
                .findByPartyRoomIdAndUserId(partyRoomId, userId)
                .map(existing -> {
                    existing.updateSlots(slots);
                    return existing;
                })
                .orElseGet(() -> Availability.create(partyRoomId, userId, slots));

        return availabilityRepository.save(availability);
    }

    /**
     * 전체 멤버 가용시간 조회 (히트맵 포함)
     */
    @Transactional(readOnly = true)
    public AllAvailabilityResponse getAllAvailability(PartyRoomId partyRoomId, UserId requesterId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND", "파티룸을 찾을 수 없습니다."));

        if (!partyRoom.isMember(requesterId)) {
            throw new DomainException("PARTY_NOT_MEMBER", "해당 파티룸의 멤버가 아닙니다.");
        }

        // 모든 멤버의 가용시간 조회
        List<Availability> allAvailabilities = availabilityRepository.findByPartyRoomId(partyRoomId);

        // AvailabilityResponse 목록 생성
        List<AvailabilityResponse> memberAvailabilities = allAvailabilities.stream()
                .map(a -> {
                    String nickname = getUserNickname(a.getUserId());
                    String characterName = getCharacterNameFromParty(partyRoom, a.getUserId());
                    return AvailabilityResponse.from(a, nickname, characterName);
                })
                .toList();

        // 히트맵 생성 (일주일 기준)
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        List<AllAvailabilityResponse.HeatmapSlot> heatmap = generateHeatmap(
                allAvailabilities, startDate, endDate);

        return new AllAvailabilityResponse(memberAvailabilities, heatmap, startDate, endDate);
    }

    /**
     * 일정 확정 (파티장만)
     */
    public PartyRoom confirmSchedule(PartyRoomId partyRoomId, UserId requesterId, Instant scheduledTime) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND", "파티룸을 찾을 수 없습니다."));

        if (!partyRoom.isLeader(requesterId)) {
            throw new DomainException("PARTY_NOT_LEADER", "파티장만 일정을 확정할 수 있습니다.");
        }

        partyRoom.setSchedule(scheduledTime);
        return partyRoomRepository.save(partyRoom);
    }

    /**
     * 히트맵 생성
     */
    private List<AllAvailabilityResponse.HeatmapSlot> generateHeatmap(
            List<Availability> allAvailabilities,
            LocalDate startDate, LocalDate endDate) {

        // 모든 시간대별로 가능한 사용자 집계
        Map<String, List<String>> slotToUsers = new HashMap<>();

        for (Availability availability : allAvailabilities) {
            for (Availability.TimeSlot slot : availability.getSlots()) {
                if (!slot.date().isBefore(startDate) && slot.date().isBefore(endDate)) {
                    String key = slot.date() + "_" + slot.time();
                    slotToUsers.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(availability.getUserId().getValue().toString());
                }
            }
        }

        // 결과 목록 생성
        List<AllAvailabilityResponse.HeatmapSlot> heatmap = new ArrayList<>();

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime time = LocalTime.of(hour, minute);
                    String key = date + "_" + time;

                    List<String> users = slotToUsers.getOrDefault(key, List.of());

                    heatmap.add(new AllAvailabilityResponse.HeatmapSlot(
                            date, time, users.size(), users
                    ));
                }
            }
        }

        return heatmap;
    }

    /**
     * 사용자 닉네임 조회
     */
    @Transactional(readOnly = true)
    public String getUserNickname(UserId userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("Unknown");
    }

    /**
     * 파티룸에서 해당 유저의 캐릭터 이름 조회
     */
    private String getCharacterNameFromParty(PartyRoom partyRoom, UserId userId) {
        return partyRoom.getActiveMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .map(PartyMember::getCharacterId)
                .flatMap(characterRepository::findById)
                .map(Character::getCharacterName)
                .orElse("Unknown");
    }

    /**
     * 파티룸과 유저 ID로 캐릭터 이름 조회 (public)
     */
    @Transactional(readOnly = true)
    public String getCharacterName(PartyRoomId partyRoomId, UserId userId) {
        PartyRoom partyRoom = partyRoomRepository.findById(partyRoomId)
                .orElseThrow(() -> new DomainException("PARTY_NOT_FOUND", "파티룸을 찾을 수 없습니다."));
        return getCharacterNameFromParty(partyRoom, userId);
    }
}
