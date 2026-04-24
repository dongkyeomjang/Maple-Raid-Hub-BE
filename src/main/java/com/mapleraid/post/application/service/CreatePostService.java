package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.external.application.port.out.NexonApiPort;
import com.mapleraid.notification.application.event.GuestPostCreatedEvent;
import com.mapleraid.post.application.port.in.input.command.CreatePostInput;
import com.mapleraid.post.application.port.in.output.result.CreatePostResult;
import com.mapleraid.post.application.port.in.usecase.CreatePostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final NexonApiPort nexonApiPort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CreatePostResult execute(CreatePostInput input) {
        if (input.isGuest()) {
            return createGuestPost(input);
        }
        return createMemberPost(input);
    }

    private CreatePostResult createMemberPost(CreatePostInput input) {
        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        if (!character.getOwnerId().equals(input.getAuthorId())) {
            throw new CommonException(ErrorCode.CHARACTER_NOT_OWNER);
        }

        if (character.getVerificationStatus() != EVerificationStatus.VERIFIED_OWNER) {
            throw new CommonException(ErrorCode.POST_REQUIRES_VERIFIED_CHARACTER);
        }

        Post post = Post.create(
                input.getAuthorId(),
                input.getCharacterId(),
                character.getEWorldGroup(),
                input.getBossIds(),
                input.getRequiredMembers(),
                input.getPreferredTime(),
                input.getDescription()
        );

        Post savedPost = postRepository.save(post);

        User author = userRepository.findById(savedPost.getAuthorId()).orElse(null);

        return CreatePostResult.from(savedPost,
                author != null ? author.getNickname() : null,
                character.getCharacterName(),
                character.getCharacterImageUrl(),
                character.getWorldName());
    }

    private CreatePostResult createGuestPost(CreatePostInput input) {
        if (input.getGuestPassword() == null || input.getGuestPassword().isBlank()) {
            throw new CommonException(ErrorCode.POST_GUEST_REQUIRES_PASSWORD);
        }

        // Nexon API로 캐릭터 존재 여부 확인 및 아바타 획득
        String ocid = nexonApiPort.resolveOcid(input.getGuestCharacterName(), input.getGuestWorldName())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_GUEST_CHARACTER_NOT_FOUND));

        NexonApiPort.CharacterBasicInfo basic = nexonApiPort.getCharacterBasic(ocid)
                .orElseThrow(() -> new CommonException(ErrorCode.POST_GUEST_CHARACTER_NOT_FOUND));

        // 입력한 월드와 실제 월드 불일치 검증
        if (basic.worldName() != null && !basic.worldName().equalsIgnoreCase(input.getGuestWorldName())) {
            throw new CommonException(ErrorCode.CHARACTER_WORLD_MISMATCH);
        }

        // 사칭 방지: 이미 인증 회원이 소유 인증을 완료한 캐릭터는 비회원 글로 작성 불가
        String resolvedCharacterName = basic.characterName() != null ? basic.characterName() : input.getGuestCharacterName();
        String resolvedWorldName = basic.worldName() != null ? basic.worldName() : input.getGuestWorldName();
        if (characterRepository.existsByNameAndWorldAndStatus(
                resolvedCharacterName, resolvedWorldName, EVerificationStatus.VERIFIED_OWNER)) {
            throw new CommonException(ErrorCode.POST_GUEST_CHARACTER_ALREADY_CLAIMED);
        }

        String passwordHash = passwordEncoder.encode(input.getGuestPassword());

        Post post = Post.createGuest(
                input.getGuestWorldGroup(),
                basic.worldName() != null ? basic.worldName() : input.getGuestWorldName(),
                basic.characterName() != null ? basic.characterName() : input.getGuestCharacterName(),
                basic.characterImage(),
                input.getContactLink(),
                passwordHash,
                input.getBossIds(),
                input.getRequiredMembers(),
                input.getPreferredTime(),
                input.getDescription()
        );

        Post savedPost = postRepository.save(post);

        // 비회원 글은 모니터링 필요 → 관리자에게 Discord DM 발송
        eventPublisher.publishEvent(new GuestPostCreatedEvent(
                savedPost.getId().getValue().toString(),
                savedPost.getGuestWorldName(),
                savedPost.getGuestCharacterName(),
                savedPost.getContactLink(),
                savedPost.getDescription(),
                savedPost.getBossIds()
        ));

        return CreatePostResult.from(savedPost, null, null, null, null);
    }
}
