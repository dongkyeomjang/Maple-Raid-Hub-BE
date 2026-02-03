package com.mapleraid.security.application.service;

import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.domain.user.User;
import com.mapleraid.security.application.dto.OauthJsonWebTokenDto;
import com.mapleraid.security.application.usecase.LoginOauthUseCase;
import com.mapleraid.security.domain.type.ESecurityRole;
import com.mapleraid.security.info.CustomTemporaryUserPrincipal;
import com.mapleraid.security.info.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginOauthService implements LoginOauthUseCase {

    private final UserRepository userRepository;
    private final JsonWebTokenUtil jsonWebTokenUtil;

    @Override
    @Transactional
    public OauthJsonWebTokenDto execute(CustomTemporaryUserPrincipal principal) {
        // 신규 가입자: 임시 토큰만 발급
        return jsonWebTokenUtil.generateOauthJsonWebTokens(
                principal.getSerialId() + ":" + principal.getProvider()
        );
    }

    @Override
    @Transactional
    public OauthJsonWebTokenDto execute(CustomUserPrincipal principal) {
        // 기존 사용자: access + refresh 토큰 발급
        User user = userRepository.findById(
                com.mapleraid.domain.user.UserId.of(principal.getId())
        ).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 로그인 기록
        user.recordLogin();
        userRepository.save(user);

        return jsonWebTokenUtil.generateOauthJsonWebTokens(
                principal.getId(),
                ESecurityRole.USER
        );
    }
}
