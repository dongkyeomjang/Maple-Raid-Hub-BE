package com.mapleraid.notification.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.notification.application.port.in.usecase.LinkDiscordUseCase;
import com.mapleraid.notification.application.port.out.DiscordApiPort;
import com.mapleraid.notification.application.port.out.NotificationPreferenceRepository;
import com.mapleraid.notification.domain.NotificationPreference;
import com.mapleraid.user.application.port.out.UserRepository;
import com.mapleraid.user.domain.User;
import com.mapleraid.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkDiscordService implements LinkDiscordUseCase {

    private static final Logger log = LoggerFactory.getLogger(LinkDiscordService.class);

    private final UserRepository userRepository;
    private final DiscordApiPort discordApiPort;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Override
    @Transactional
    public void execute(UserId userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        if (user.isDiscordLinked()) {
            throw new CommonException(ErrorCode.DISCORD_ALREADY_LINKED);
        }

        try {
            DiscordApiPort.DiscordTokenResponse tokenResponse = discordApiPort.exchangeCode(code);
            DiscordApiPort.DiscordUserInfo userInfo = discordApiPort.getCurrentUser(tokenResponse.accessToken());

            // 이미 다른 유저에게 연동된 Discord 계정인지 확인
            userRepository.findByDiscordId(userInfo.id()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new CommonException(ErrorCode.DISCORD_ID_IN_USE);
                }
            });

            // 알림 서버 자동 가입 (실패해도 연동은 진행)
            try {
                discordApiPort.addGuildMember(userInfo.id(), tokenResponse.accessToken());
            } catch (Exception e) {
                log.warn("[Discord] 알림 서버 자동 가입 실패 (연동은 계속 진행) userId={}: {}", userId.getValue(), e.getMessage());
            }

            user.linkDiscord(userInfo.id(), userInfo.globalName());
            userRepository.save(user);

            // 알림 설정 기본값 생성 (없는 경우)
            notificationPreferenceRepository.findByUserId(userId)
                    .orElseGet(() -> notificationPreferenceRepository.save(
                            NotificationPreference.createDefault(userId)));

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Discord] 연동 실패 userId={}: {}", userId.getValue(), e.getMessage(), e);
            throw new CommonException(ErrorCode.DISCORD_LINK_FAILED);
        }
    }
}
