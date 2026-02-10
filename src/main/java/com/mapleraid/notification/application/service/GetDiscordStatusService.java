package com.mapleraid.notification.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.notification.application.port.in.usecase.GetDiscordStatusUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDiscordStatusService implements GetDiscordStatusUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DiscordStatus execute(UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        return new DiscordStatus(user.isDiscordLinked(), user.getDiscordUsername());
    }
}
