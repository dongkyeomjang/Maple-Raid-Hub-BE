package com.mapleraid.party.adapter.out.persistence;

import com.mapleraid.party.adapter.out.persistence.jpa.AvailabilityJpaEntity;
import com.mapleraid.party.adapter.out.persistence.jpa.AvailabilityJpaRepository;
import com.mapleraid.party.application.port.out.AvailabilityRepository;
import com.mapleraid.party.domain.Availability;
import com.mapleraid.party.domain.AvailabilityId;
import com.mapleraid.party.domain.PartyRoomId;
import com.mapleraid.user.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AvailabilityPersistenceAdapter implements AvailabilityRepository {

    private final AvailabilityJpaRepository jpaRepository;

    public AvailabilityPersistenceAdapter(AvailabilityJpaRepository jpaRepository) {
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
