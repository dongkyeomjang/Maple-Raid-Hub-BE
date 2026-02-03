package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.UserJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.UserJpaRepository;
import com.mapleraid.application.port.out.UserRepository;
import com.mapleraid.domain.user.User;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return jpaRepository.findByNickname(nickname)
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaRepository.existsByNickname(nickname);
    }

    @Override
    public Map<UserId, User> findAllByIds(List<UserId> ids) {
        List<String> stringIds = ids.stream()
                .map(id -> id.getValue().toString())
                .toList();

        return jpaRepository.findAllById(stringIds).stream()
                .map(UserJpaEntity::toDomain)
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(UserJpaEntity::toDomain);
    }
}
