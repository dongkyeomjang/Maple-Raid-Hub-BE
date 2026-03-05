package com.mapleraid.party.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.in.input.command.KickMemberInput;
import com.mapleraid.party.application.port.in.usecase.KickMemberUseCase;
import com.mapleraid.party.application.port.out.AvailabilityRepository;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.post.application.port.out.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KickMemberService implements KickMemberUseCase {
    private final PartyRoomRepository partyRoomRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void execute(KickMemberInput input) {
        PartyRoom partyRoom = partyRoomRepository.findById(input.getPartyRoomId())
                .orElseThrow(() -> new CommonException(ErrorCode.PARTY_NOT_FOUND));

        partyRoom.kickMember(input.getRequesterId(), input.getTargetUserId());

        // 추방된 멤버의 가용시간 삭제
        availabilityRepository.deleteByPartyRoomIdAndUserId(input.getPartyRoomId(), input.getTargetUserId());

        // 모집글의 파티원 추방 처리 (인원 감소 + 지원 상태 변경)
        postRepository.findByIdWithApplications(partyRoom.getPostId())
                .ifPresent(post -> {
                    post.memberLeft(input.getTargetUserId());
                    postRepository.save(post);
                });

        partyRoomRepository.save(partyRoom);
    }
}
