package com.mapleraid.security.application.service;

import com.mapleraid.security.info.CustomTemporaryUserPrincipal;
import com.mapleraid.security.info.CustomUserPrincipal;
import com.mapleraid.security.info.factory.Oauth2UserInfo;
import com.mapleraid.security.info.factory.Oauth2UserInfoFactory;
import com.mapleraid.security.type.ESecurityProvider;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserDetailService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // provider 가져오기
        ESecurityProvider provider = ESecurityProvider.fromString(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        // 사용자 정보 가져오기
        Oauth2UserInfo oauth2UserInfo = Oauth2UserInfoFactory.getOauth2UserInfo(
                provider,
                super.loadUser(userRequest).getAttributes()
        );

        // 이미 존재하는 사용자인지 확인
        User user = userRepository.findByProviderAndProviderId(
                provider.name().toLowerCase(),
                oauth2UserInfo.getId()
        ).orElse(null);

        // 최초 가입 유저라면 CustomTemporaryUserPrincipal 반환
        if (user == null) {
            return new CustomTemporaryUserPrincipal(oauth2UserInfo.getId(), provider);
        }

        // 기존 유저라면 CustomUserPrincipal 반환
        return CustomUserPrincipal.create(user, oauth2UserInfo.getAttributes());
    }
}
