package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.PartyRoomJpaEntity;
import com.mapleraid.domain.party.PartyRoomStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyRoomJpaRepository extends JpaRepository<PartyRoomJpaEntity, String> {

    @EntityGraph(value = "PartyRoom.withMembersAndBossIds")
    @Query("SELECT pr FROM PartyRoomJpaEntity pr WHERE pr.id = :id")
    Optional<PartyRoomJpaEntity> findByIdWithMembers(@Param("id") String id);

    @EntityGraph(value = "PartyRoom.withMembersAndBossIds")
    @Query("SELECT pr FROM PartyRoomJpaEntity pr WHERE pr.postId = :postId")
    Optional<PartyRoomJpaEntity> findByPostId(@Param("postId") String postId);

    @EntityGraph(value = "PartyRoom.withMembersAndBossIds")
    @Query("SELECT DISTINCT pr FROM PartyRoomJpaEntity pr LEFT JOIN pr.members m " +
            "WHERE m.userId = :userId AND m.leftAt IS NULL")
    List<PartyRoomJpaEntity> findByMemberUserId(@Param("userId") String userId);

    @EntityGraph(value = "PartyRoom.withMembersAndBossIds")
    @Query("SELECT DISTINCT pr FROM PartyRoomJpaEntity pr LEFT JOIN pr.members m " +
            "WHERE m.userId = :userId AND m.leftAt IS NULL AND pr.status = :status")
    List<PartyRoomJpaEntity> findByMemberUserIdAndStatus(
            @Param("userId") String userId, @Param("status") PartyRoomStatus status);
}
