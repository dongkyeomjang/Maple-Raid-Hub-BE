package com.mapleraid.security.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.port.in.input.command.UpdateNicknameInput;
import com.mapleraid.security.application.port.in.output.result.UpdateNicknameResult;
import com.mapleraid.security.application.port.in.usecase.UpdateNicknameUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UpdateNicknameService implements UpdateNicknameUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UpdateNicknameResult execute(UpdateNicknameInput input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 변경 시 중복 체크
        if (!user.getNickname().equals(input.getNickname())
                && userRepository.existsByNickname(input.getNickname())) {
            throw new CommonException(ErrorCode.AUTH_NICKNAME_DUPLICATE);
        }

        user.updateNickname(input.getNickname());
        User savedUser = userRepository.save(user);
        return UpdateNicknameResult.from(savedUser);
    }
}
