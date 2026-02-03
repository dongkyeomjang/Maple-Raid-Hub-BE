package com.mapleraid.application.port.out;

import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Map<UserId, User> findAllByIds(List<UserId> ids);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
