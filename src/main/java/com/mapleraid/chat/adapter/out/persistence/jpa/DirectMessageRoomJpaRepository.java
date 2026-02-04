package com.mapleraid.chat.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRoomJpaRepository extends JpaRepository<DirectMessageRoomJpaEntity, String> {

    @Query("SELECT r FROM DirectMessageRoomJpaEntity r " +
            "WHERE r.user1Id = :userId OR r.user2Id = :userId " +
            "ORDER BY r.lastMessageAt DESC NULLS LAST")
    List<DirectMessageRoomJpaEntity> findByUserId(@Param("userId") String userId);

    @Query("SELECT r FROM DirectMessageRoomJpaEntity r " +
            "WHERE r.postId = :postId " +
            "AND ((r.user1Id = :user1Id AND r.user2Id = :user2Id) " +
            "  OR (r.user1Id = :user2Id AND r.user2Id = :user1Id))")
    Optional<DirectMessageRoomJpaEntity> findByPostIdAndUsers(
            @Param("postId") String postId,
            @Param("user1Id") String user1Id,
            @Param("user2Id") String user2Id);

    @Query("SELECT r FROM DirectMessageRoomJpaEntity r " +
            "WHERE r.postId IS NULL " +
            "AND ((r.user1Id = :user1Id AND r.user2Id = :user2Id) " +
            "  OR (r.user1Id = :user2Id AND r.user2Id = :user1Id))")
    Optional<DirectMessageRoomJpaEntity> findByUsersWithoutPost(
            @Param("user1Id") String user1Id,
            @Param("user2Id") String user2Id);
}
