package com.mapleraid.security.info;

import com.mapleraid.security.type.ESecurityRole;
import com.mapleraid.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Builder
@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails, OAuth2User {

    @Getter
    private final UUID id;
    @Getter
    private final ESecurityRole role;
    private final String password;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public static CustomUserPrincipal create(User user) {
        return CustomUserPrincipal.builder()
                .id(user.getId().getValue())
                .role(ESecurityRole.USER)
                .password(user.getPasswordHash())
                .attributes(Collections.emptyMap())
                .authorities(Collections.singleton(new SimpleGrantedAuthority(ESecurityRole.USER.getSecurityName())))
                .build();
    }

    public static CustomUserPrincipal create(User user, Map<String, Object> attributes) {
        return CustomUserPrincipal.builder()
                .id(user.getId().getValue())
                .role(ESecurityRole.USER)
                .password(user.getPasswordHash())
                .attributes(attributes)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(ESecurityRole.USER.getSecurityName())))
                .build();
    }

    @Override
    public String getUsername() {
        return id.toString();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return id.toString();
    }
}
