package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.AvailabilityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvailabilityJpaRepository extends JpaRepository<AvailabilityJpaEntity, String> {

    List<AvailabilityJpaEntity> findByPartyRoomId(String partyRoomId);

    Optional<AvailabilityJpaEntity> findByPartyRoomIdAndUserId(String partyRoomId, String userId);

    void deleteByPartyRoomIdAndUserId(String partyRoomId, String userId);
}
