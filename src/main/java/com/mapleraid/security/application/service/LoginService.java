package com.mapleraid.security.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.port.in.input.command.LoginInput;
import com.mapleraid.security.application.port.in.output.result.LoginResult;
import com.mapleraid.security.application.port.in.usecase.LoginUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResult execute(LoginInput input) {
        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new CommonException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new CommonException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        user.recordLogin();
        User savedUser = userRepository.save(user);
        return LoginResult.from(savedUser);
    }
}
