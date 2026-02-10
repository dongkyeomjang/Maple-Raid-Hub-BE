package com.mapleraid.party.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.query.ReadAllAvailabilityInput;
import com.mapleraid.party.application.port.in.output.result.ReadAllAvailabilityResult;
import com.mapleraid.party.application.port.in.usecase.ReadAllAvailabilityUseCase;
import com.mapleraid.party.application.port.out.AvailabilityRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.PartyMember;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReadAllAvailabilityService implements ReadAllAvailabilityUseCase {
    private final AvailabilityRepository availabilityRepository;
    private final PartyRoomRepository partyRoomRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadAllAvailabilityResult execute(ReadAllAvailabilityInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));
        if (!partyRoom.isMember(input.getRequesterId())) {
            throw new CommonException(ErrorCode.PARTY_NOT_MEMBER);
        }
        List<Availability> allAvailabilities = availabilityRepository.findByPartyRoomId(input.getPartyRoomId());
        List<ReadAllAvailabilityResult.MemberAvailability> memberAvails = allAvailabilities.stream()
                .map(a -> {
                    String nickname = userRepository.findById(a.getUserId())
                            .map(User::getNickname).orElse("Unknown");
                    String charName = getCharacterNameFromParty(partyRoom, a.getUserId());
                    List<ReadAllAvailabilityResult.TimeSlotDto> slots = a.getSlots().stream()
                            .map(s -> new ReadAllAvailabilityResult.TimeSlotDto(s.date(), s.time()))
                            .toList();
                    return new ReadAllAvailabilityResult.MemberAvailability(
                            a.getId().getValue().toString(),
                            a.getPartyRoomId().getValue().toString(),
                            a.getUserId().getValue().toString(),
                            nickname, charName, slots, a.getUpdatedAt());
                }).toList();

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        List<ReadAllAvailabilityResult.HeatmapSlot> heatmap = generateHeatmap(allAvailabilities, startDate, endDate);
        return new ReadAllAvailabilityResult(memberAvails, heatmap, startDate, endDate);
    }

    private String getCharacterNameFromParty(PartyRoom partyRoom, UserId userId) {
        return partyRoom.getActiveMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .map(PartyMember::getCharacterId)
                .flatMap(characterRepository::findById)
                .map(Character::getCharacterName)
                .orElse("Unknown");
    }

    private List<ReadAllAvailabilityResult.HeatmapSlot> generateHeatmap(
            List<Availability> allAvailabilities, LocalDate startDate, LocalDate endDate) {
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
        List<ReadAllAvailabilityResult.HeatmapSlot> heatmap = new ArrayList<>();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime time = LocalTime.of(hour, minute);
                    String key = date + "_" + time;
                    List<String> users = slotToUsers.getOrDefault(key, List.of());
                    heatmap.add(new ReadAllAvailabilityResult.HeatmapSlot(date, time, users.size(), users));
                }
            }
        }
        return heatmap;
    }
}
