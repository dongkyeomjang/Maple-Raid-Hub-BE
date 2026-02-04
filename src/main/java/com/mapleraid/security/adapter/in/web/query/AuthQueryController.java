package com.mapleraid.security.adapter.in.web.query;

import com.mapleraid.core.annotation.bean.CurrentUser;
import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.security.adapter.in.web.dto.response.UserResponseDto;
import com.mapleraid.security.application.port.in.input.query.ReadCurrentUserInput;
import com.mapleraid.security.application.port.in.usecase.ReadCurrentUserUseCase;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthQueryController {

    private final ReadCurrentUserUseCase readCurrentUserUseCase;

    @GetMapping("/me")
    public ResponseDto<UserResponseDto> getCurrentUser(@CurrentUser UserId userId) {
        return ResponseDto.ok(
                UserResponseDto.from(
                        readCurrentUserUseCase.execute(
                                ReadCurrentUserInput.of(userId)
                        )
                )
        );
    }
}
