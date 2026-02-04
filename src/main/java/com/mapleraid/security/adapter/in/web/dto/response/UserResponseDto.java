package com.mapleraid.security.adapter.in.web.dto.response;

import com.mapleraid.security.application.port.in.output.result.CompleteOauthSignupResult;
import com.mapleraid.security.application.port.in.output.result.LoginResult;
import com.mapleraid.security.application.port.in.output.result.ReadCurrentUserResult;
import com.mapleraid.security.application.port.in.output.result.SignupResult;
import com.mapleraid.security.application.port.in.output.result.UpdateNicknameResult;

import java.time.LocalDateTime;

public record UserResponseDto(
        String id,
        String username,
        String nickname,
        boolean nicknameSet,
        double temperature,
        int completedParties,
        LocalDateTime createdAt
) {
    public static UserResponseDto from(SignupResult result) {
        return new UserResponseDto(
                result.getUserId(),
                result.getUsername(),
                result.getNickname(),
                result.isNicknameSet(),
                result.getTemperature(),
                result.getCompletedParties(),
                result.getCreatedAt()
        );
    }

    public static UserResponseDto from(LoginResult result) {
        return new UserResponseDto(
                result.getUserId(),
                result.getUsername(),
                result.getNickname(),
                result.isNicknameSet(),
                result.getTemperature(),
                result.getCompletedParties(),
                result.getCreatedAt()
        );
    }

    public static UserResponseDto from(CompleteOauthSignupResult result) {
        return new UserResponseDto(
                result.getUserId(),
                result.getUsername(),
                result.getNickname(),
                result.isNicknameSet(),
                result.getTemperature(),
                result.getCompletedParties(),
                result.getCreatedAt()
        );
    }

    public static UserResponseDto from(ReadCurrentUserResult result) {
        return new UserResponseDto(
                result.getUserId(),
                result.getUsername(),
                result.getNickname(),
                result.isNicknameSet(),
                result.getTemperature(),
                result.getCompletedParties(),
                result.getCreatedAt()
        );
    }

    public static UserResponseDto from(UpdateNicknameResult result) {
        return new UserResponseDto(
                result.getUserId(),
                result.getUsername(),
                result.getNickname(),
                result.isNicknameSet(),
                result.getTemperature(),
                result.getCompletedParties(),
                result.getCreatedAt()
        );
    }
}
