package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.AvailabilityJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.AvailabilityJpaRepository;
import com.mapleraid.application.port.out.AvailabilityRepository;
import com.mapleraid.domain.party.PartyRoomId;
import com.mapleraid.domain.partyroom.Availability;
import com.mapleraid.domain.partyroom.AvailabilityId;
import com.mapleraid.domain.user.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AvailabilityRepositoryAdapter implements AvailabilityRepository {

    private final AvailabilityJpaRepository jpaRepository;

    public AvailabilityRepositoryAdapter(AvailabilityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Availability save(Availability availability) {
        AvailabilityJpaEntity entity = AvailabilityJpaEntity.fromDomain(availability);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Availability> findById(AvailabilityId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(AvailabilityJpaEntity::toDomain);
    }

    @Override
    public List<Availability> findByPartyRoomId(PartyRoomId partyRoomId) {
        return jpaRepository.findByPartyRoomId(partyRoomId.getValue().toString()).stream()
                .map(AvailabilityJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Availability> findByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId) {
        return jpaRepository.findByPartyRoomIdAndUserId(
                partyRoomId.getValue().toString(),
                userId.getValue().toString()
        ).map(AvailabilityJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void deleteByPartyRoomIdAndUserId(PartyRoomId partyRoomId, UserId userId) {
        jpaRepository.deleteByPartyRoomIdAndUserId(
                partyRoomId.getValue().toString(),
                userId.getValue().toString()
        );
    }
}
