package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.post.application.port.in.input.command.AcceptApplicationInput;
import com.mapleraid.post.application.port.in.output.result.AcceptApplicationResult;
import com.mapleraid.post.application.port.in.usecase.AcceptApplicationUseCase;
import com.mapleraid.notification.application.event.ApplicationAcceptedEvent;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcceptApplicationService implements AcceptApplicationUseCase {

    private final PostRepository postRepository;
    private final PartyRoomRepository partyRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AcceptApplicationResult execute(AcceptApplicationInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        post.acceptApplication(input.getApplicationId());

        PartyRoom partyRoom = partyRoomRepository.findByPostId(input.getPostId())
                .orElseGet(() -> {
                    PartyRoom newRoom = PartyRoom.create(
                            post.getId(),
                            post.getBossIds(),
                            post.getAuthorId(),
                            post.getCharacterId()
                    );

                    post.linkPartyRoom(newRoom.getId());
                    return partyRoomRepository.save(newRoom);
                });

        Application acceptedApp = post.getApplications().stream()
                .filter(app -> app.getId().equals(input.getApplicationId()))
                .findFirst()
                .orElseThrow();

        partyRoom.addMember(
                acceptedApp.getApplicantId(),
                acceptedApp.getCharacterId()
        );

        partyRoomRepository.save(partyRoom);
        postRepository.save(post);

        messagingTemplate.convertAndSend(
                "/topic/post/" + input.getPostId().getValue(),
                Map.of("type", "APPLICATION_ACCEPTED",
                        "postId", input.getPostId().getValue().toString(),
                        "applicationId", input.getApplicationId().getValue().toString()));

        // 모든 파티 멤버에게 새 파티방 알림 전송 (채팅방 목록 갱신용)
        partyRoom.getActiveMemberIds().forEach(memberId ->
                messagingTemplate.convertAndSendToUser(
                        memberId.getValue().toString(),
                        "/queue/party-room-updates",
                        Map.of("type", "PARTY_ROOM_UPDATED",
                                "partyRoomId", partyRoom.getId().getValue().toString())));

        String bossName = post.getBossIds().isEmpty() ? "" : post.getBossIds().get(0);
        eventPublisher.publishEvent(new ApplicationAcceptedEvent(
                acceptedApp.getApplicantId(),
                bossName,
                partyRoom.getId().getValue().toString()
        ));

        return AcceptApplicationResult.from(acceptedApp, partyRoom);
    }
}
