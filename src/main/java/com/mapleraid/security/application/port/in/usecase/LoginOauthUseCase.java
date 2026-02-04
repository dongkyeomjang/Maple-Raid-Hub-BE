package com.mapleraid.security.application.port.in.usecase;

import com.mapleraid.security.application.dto.OauthJsonWebTokenDto;
import com.mapleraid.security.info.CustomTemporaryUserPrincipal;
import com.mapleraid.security.info.CustomUserPrincipal;

public interface LoginOauthUseCase {

    OauthJsonWebTokenDto execute(CustomTemporaryUserPrincipal principal);

    OauthJsonWebTokenDto execute(CustomUserPrincipal principal);
}
