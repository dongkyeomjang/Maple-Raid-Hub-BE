package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.party.application.port.out.PartyRoomRepository;
import com.mapleraid.party.domain.PartyRoom;
import com.mapleraid.post.application.port.in.input.command.DeletePostInput;
import com.mapleraid.post.application.port.in.usecase.DeletePostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostService implements DeletePostUseCase {

    private final PostRepository postRepository;
    private final PartyRoomRepository partyRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(DeletePostInput input) {
        Post post = postRepository.findById(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (post.isGuest()) {
            if (input.getGuestPassword() == null
                    || !passwordEncoder.matches(input.getGuestPassword(), post.getGuestPasswordHash())) {
                throw new CommonException(ErrorCode.POST_GUEST_INVALID_PASSWORD);
            }
            // 비회원 글은 파티룸이 생성될 일이 없으니 곧바로 취소
            post.cancel();
            postRepository.save(post);
            return;
        }

        if (input.getRequesterId() == null || !post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        // 연결된 파티룸이 있으면 파티장 혼자인 경우만 취소 가능
        if (post.getPartyRoomId() != null) {
            PartyRoom partyRoom = partyRoomRepository.findById(post.getPartyRoomId())
                    .orElse(null);
            if (partyRoom != null && partyRoom.getActiveMembers().size() > 1) {
                throw new CommonException(ErrorCode.POST_HAS_PARTY_ROOM);
            }
            post.cancel();
            postRepository.save(post);
            if (partyRoom != null) {
                partyRoom.complete(input.getRequesterId());
                partyRoomRepository.save(partyRoom);
            }
        } else {
            post.cancel();
            postRepository.save(post);
        }
    }
}
