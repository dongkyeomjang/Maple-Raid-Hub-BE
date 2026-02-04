package com.mapleraid.security.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.port.in.input.query.ReadCurrentUserInput;
import com.mapleraid.security.application.port.in.output.result.ReadCurrentUserResult;
import com.mapleraid.security.application.port.in.usecase.ReadCurrentUserUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadCurrentUserService implements ReadCurrentUserUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadCurrentUserResult execute(ReadCurrentUserInput input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        return ReadCurrentUserResult.from(user);
    }
}
