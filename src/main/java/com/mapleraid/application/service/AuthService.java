package com.mapleraid.application.service;

import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.common.DomainException;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입
     */
    public User signup(String username, String password, String nickname) {
        // 아이디 중복 체크
        if (userRepository.existsByUsername(username)) {
            throw new DomainException("AUTH_USERNAME_DUPLICATE",
                    "이미 사용 중인 아이디입니다.",
                    Map.of("username", username));
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new DomainException("AUTH_NICKNAME_DUPLICATE",
                    "이미 사용 중인 닉네임입니다.",
                    Map.of("nickname", nickname));
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 생성
        User user = User.create(
                UserId.generate(),
                username,
                encodedPassword,
                nickname
        );

        return userRepository.save(user);
    }

    /**
     * 로그인
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("AUTH_INVALID_CREDENTIALS",
                        "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new DomainException("AUTH_INVALID_CREDENTIALS",
                    "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    /**
     * 사용자 조회
     */
    @Transactional(readOnly = true)
    public User getUser(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND",
                        "사용자를 찾을 수 없습니다."));
    }

    /**
     * 프로필 수정
     */
    public User updateProfile(UserId userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND",
                        "사용자를 찾을 수 없습니다."));

        // 닉네임 변경 시 중복 체크
        if (!user.getNickname().equals(nickname) && userRepository.existsByNickname(nickname)) {
            throw new DomainException("AUTH_NICKNAME_DUPLICATE",
                    "이미 사용 중인 닉네임입니다.",
                    Map.of("nickname", nickname));
        }

        user.updateNickname(nickname);
        return userRepository.save(user);
    }

    /**
     * OAuth 회원가입
     */
    public User signupOAuth(String provider, String providerId, String nickname) {
        // 이미 가입된 사용자인지 확인
        if (userRepository.findByProviderAndProviderId(provider, providerId).isPresent()) {
            throw new DomainException("AUTH_ALREADY_EXISTS",
                    "이미 가입된 사용자입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new DomainException("AUTH_NICKNAME_DUPLICATE",
                    "이미 사용 중인 닉네임입니다.",
                    Map.of("nickname", nickname));
        }

        // 랜덤 비밀번호 생성 및 암호화
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        // OAuth 사용자 생성
        User user = User.createOAuthUser(
                UserId.generate(),
                provider,
                providerId,
                nickname,
                randomPassword
        );

        // 닉네임 설정 완료 처리
        user.updateNickname(nickname);

        return userRepository.save(user);
    }
}
