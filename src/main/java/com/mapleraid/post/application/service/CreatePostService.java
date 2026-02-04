package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.character.domain.type.EVerificationStatus;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.CreatePostInput;
import com.mapleraid.post.application.port.in.output.result.CreatePostResult;
import com.mapleraid.post.application.port.in.usecase.CreatePostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    public CreatePostService(PostRepository postRepository,
                             CharacterRepository characterRepository,
                             UserRepository userRepository) {
        this.postRepository = postRepository;
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CreatePostResult execute(CreatePostInput input) {
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
                character.getCharacterImageUrl());
    }
}
