package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.DirectMessageJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageJpaRepository extends JpaRepository<DirectMessageJpaEntity, String> {

    @Query("SELECT m FROM DirectMessageJpaEntity m " +
            "WHERE m.roomId = :roomId " +
            "ORDER BY m.createdAt DESC")
    Page<DirectMessageJpaEntity> findByRoomId(@Param("roomId") String roomId, Pageable pageable);

    @Query("SELECT m FROM DirectMessageJpaEntity m " +
            "WHERE m.roomId = :roomId " +
            "ORDER BY m.createdAt ASC")
    List<DirectMessageJpaEntity> findByRoomIdOrderByCreatedAtAsc(@Param("roomId") String roomId);

    @Modifying
    @Query("UPDATE DirectMessageJpaEntity m SET m.isRead = true " +
            "WHERE m.roomId = :roomId AND m.senderId != :userId AND m.isRead = false")
    int markAsReadByRoomIdAndNotSender(
            @Param("roomId") String roomId,
            @Param("userId") String userId);
}
