package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.VerifyGuestPasswordInput;
import com.mapleraid.post.application.port.in.usecase.VerifyGuestPasswordUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyGuestPasswordService implements VerifyGuestPasswordUseCase {

    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public void execute(VerifyGuestPasswordInput input) {
        Post post = postRepository.findById(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isGuest()) {
            throw new CommonException(ErrorCode.POST_NOT_GUEST);
        }

        if (input.getPassword() == null
                || !passwordEncoder.matches(input.getPassword(), post.getGuestPasswordHash())) {
            throw new CommonException(ErrorCode.POST_GUEST_INVALID_PASSWORD);
        }
    }
}
