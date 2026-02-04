package com.mapleraid.security.info.factory;

import com.mapleraid.security.info.KakaoOauth2UserInfo;
import com.mapleraid.security.type.ESecurityProvider;

import java.util.Map;

public class Oauth2UserInfoFactory {

    public static Oauth2UserInfo getOauth2UserInfo(ESecurityProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case KAKAO -> new KakaoOauth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("잘못된 제공자입니다: " + provider);
        };
    }
}
