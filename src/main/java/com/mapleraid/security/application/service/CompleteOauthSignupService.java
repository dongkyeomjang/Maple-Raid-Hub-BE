package com.mapleraid.security.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.port.in.input.command.CompleteOauthSignupInput;
import com.mapleraid.security.application.port.in.output.result.CompleteOauthSignupResult;
import com.mapleraid.security.application.port.in.usecase.CompleteOauthSignupUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompleteOauthSignupService implements CompleteOauthSignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CompleteOauthSignupResult execute(CompleteOauthSignupInput input) {
        String provider = input.getProvider().name().toLowerCase();
        String providerId = input.getProviderId();

        // 이미 가입된 사용자인지 확인
        if (userRepository.findByProviderAndProviderId(provider, providerId).isPresent()) {
            throw new CommonException(ErrorCode.AUTH_ALREADY_EXISTS);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(input.getNickname())) {
            throw new CommonException(ErrorCode.AUTH_NICKNAME_DUPLICATE);
        }

        // 랜덤 비밀번호 생성 및 암호화
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        // OAuth 사용자 생성
        User user = User.createOAuthUser(
                UserId.generate(),
                provider,
                providerId,
                input.getNickname(),
                randomPassword
        );

        // 닉네임 설정 완료 처리
        user.updateNickname(input.getNickname());

        User savedUser = userRepository.save(user);
        return CompleteOauthSignupResult.from(savedUser);
    }
}
