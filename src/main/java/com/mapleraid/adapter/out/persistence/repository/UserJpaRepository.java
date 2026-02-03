package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByNickname(String nickname);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<UserJpaEntity> findByProviderAndProviderId(String provider, String providerId);

    @Query("SELECT AVG(u.temperature) FROM UserJpaEntity u")
    BigDecimal findAverageTemperature();
}
