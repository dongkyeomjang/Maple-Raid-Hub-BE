package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.UpdatePostInput;
import com.mapleraid.post.application.port.in.output.result.UpdatePostResult;
import com.mapleraid.post.application.port.in.usecase.UpdatePostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostService implements UpdatePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    @Override
    @Transactional
    public UpdatePostResult execute(UpdatePostInput input) {
        Post post = postRepository.findById(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        post.update(
                input.getBossIds(),
                input.getRequiredMembers(),
                input.getPreferredTime(),
                input.isClearPreferredTime(),
                input.getDescription(),
                input.isClearDescription()
        );

        Post savedPost = postRepository.save(post);

        User author = userRepository.findById(savedPost.getAuthorId()).orElse(null);
        Character character = characterRepository.findById(savedPost.getCharacterId()).orElse(null);

        return UpdatePostResult.from(savedPost,
                author != null ? author.getNickname() : null,
                character != null ? character.getCharacterName() : null,
                character != null ? character.getCharacterImageUrl() : null);
    }
}
