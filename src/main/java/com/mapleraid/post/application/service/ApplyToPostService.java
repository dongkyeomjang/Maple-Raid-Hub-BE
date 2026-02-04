package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.ApplyToPostInput;
import com.mapleraid.post.application.port.in.output.result.ApplyToPostResult;
import com.mapleraid.post.application.port.in.usecase.ApplyToPostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ApplyToPostService implements ApplyToPostUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ApplyToPostService(PostRepository postRepository,
                              CharacterRepository characterRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.postRepository = postRepository;
        this.characterRepository = characterRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public ApplyToPostResult execute(ApplyToPostInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        Character character = characterRepository.findById(input.getCharacterId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CHARACTER));

        if (!character.getOwnerId().equals(input.getApplicantId())) {
            throw new CommonException(ErrorCode.CHARACTER_NOT_OWNER);
        }

        if (character.getVerificationStatus() != EVerificationStatus.VERIFIED_OWNER) {
            throw new CommonException(ErrorCode.APPLICATION_REQUIRES_VERIFIED_CHARACTER);
        }

        Application application = post.apply(
                input.getApplicantId(),
                input.getCharacterId(),
                character.getEWorldGroup(),
                input.getMessage()
        );

        postRepository.save(post);

        messagingTemplate.convertAndSend(
                "/topic/post/" + input.getPostId().getValue(),
                Map.of("type", "APPLICATION_NEW", "postId", input.getPostId().getValue().toString()));

        return ApplyToPostResult.from(application);
    }
}
