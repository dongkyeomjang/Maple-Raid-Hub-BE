package com.mapleraid.security.application.service;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.core.utility.JsonWebTokenUtil;
import com.mapleraid.security.application.port.in.input.command.ResetPasswordInput;
import com.mapleraid.security.application.port.in.usecase.ResetPasswordUseCase;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final JsonWebTokenUtil jsonWebTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(ResetPasswordInput input) {
        // 복구 토큰 검증
        Claims claims;
        try {
            claims = jsonWebTokenUtil.validateToken(input.getRecoveryToken());
        } catch (CommonException e) {
            throw new CommonException(ErrorCode.RECOVERY_INVALID_TOKEN);
        }

        String tokenData = claims.get(Constants.ACCOUNT_ID_CLAIM_NAME, String.class);
        if (tokenData == null) {
            throw new CommonException(ErrorCode.RECOVERY_INVALID_TOKEN);
        }

        String[] parts = tokenData.split(":");
        if (parts.length != 2 || !"recovery".equals(parts[1])) {
            throw new CommonException(ErrorCode.RECOVERY_INVALID_TOKEN);
        }

        String userId = parts[0];

        // 사용자 찾기
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        // 토큰 발급 이후 비밀번호가 이미 변경되었는지 확인 (재사용 방지)
        Date issuedAt = claims.getIssuedAt();
        if (issuedAt != null && user.getUpdatedAt() != null
                && user.getUpdatedAt().isAfter(issuedAt.toInstant())) {
            throw new CommonException(ErrorCode.RECOVERY_INVALID_TOKEN);
        }

        // 비밀번호 변경
        String newPasswordHash = passwordEncoder.encode(input.getNewPassword());
        user.changePassword(newPasswordHash);
        userRepository.save(user);
    }
}
