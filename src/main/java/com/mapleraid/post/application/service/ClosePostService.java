package com.mapleraid.post.application.service;

import com.mapleraid.character.application.port.out.CharacterRepository;
import com.mapleraid.character.domain.Character;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.ClosePostInput;
import com.mapleraid.post.application.port.in.output.result.ClosePostResult;
import com.mapleraid.post.application.port.in.usecase.ClosePostUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClosePostService implements ClosePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public ClosePostService(PostRepository postRepository,
                            UserRepository userRepository,
                            CharacterRepository characterRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public ClosePostResult execute(ClosePostInput input) {
        Post post = postRepository.findById(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        post.close();

        Post savedPost = postRepository.save(post);

        User author = userRepository.findById(savedPost.getAuthorId()).orElse(null);
        Character character = characterRepository.findById(savedPost.getCharacterId()).orElse(null);

        return ClosePostResult.from(savedPost,
                author != null ? author.getNickname() : null,
                character != null ? character.getCharacterName() : null,
                character != null ? character.getCharacterImageUrl() : null);
    }
}
