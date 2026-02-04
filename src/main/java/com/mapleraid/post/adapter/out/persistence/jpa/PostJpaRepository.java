package com.mapleraid.post.adapter.out.persistence.jpa;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.post.domain.ApplicationStatus;
import com.mapleraid.post.domain.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, String> {

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications WHERE p.id = :id")
    Optional<PostJpaEntity> findByIdWithApplications(@Param("id") String id);

    List<PostJpaEntity> findByWorldGroupAndStatus(EWorldGroup worldGroup, PostStatus status, Pageable pageable);

    List<PostJpaEntity> findByStatus(PostStatus status, Pageable pageable);

    long countByWorldGroupAndStatus(EWorldGroup worldGroup, PostStatus status);

    long countByStatus(PostStatus status);

    List<PostJpaEntity> findByAuthorId(String authorId);

    List<PostJpaEntity> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    List<PostJpaEntity> findByAuthorIdAndStatusOrderByCreatedAtDesc(String authorId, PostStatus status);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p WHERE p.status = :status " +
           "AND (SELECT COUNT(DISTINCT b) FROM PostJpaEntity p2 JOIN p2.bossIds b " +
           "WHERE p2 = p AND b IN :bossIds) = :bossIdCount")
    List<PostJpaEntity> findByStatusAndBossIdsContainingAll(
            @Param("status") PostStatus status,
            @Param("bossIds") List<String> bossIds,
            @Param("bossIdCount") long bossIdCount,
            Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM PostJpaEntity p WHERE p.status = :status " +
           "AND (SELECT COUNT(DISTINCT b) FROM PostJpaEntity p2 JOIN p2.bossIds b " +
           "WHERE p2 = p AND b IN :bossIds) = :bossIdCount")
    long countByStatusAndBossIdsContainingAll(
            @Param("status") PostStatus status,
            @Param("bossIds") List<String> bossIds,
            @Param("bossIdCount") long bossIdCount);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p WHERE p.worldGroup = :worldGroup AND p.status = :status " +
           "AND (SELECT COUNT(DISTINCT b) FROM PostJpaEntity p2 JOIN p2.bossIds b " +
           "WHERE p2 = p AND b IN :bossIds) = :bossIdCount")
    List<PostJpaEntity> findByWorldGroupAndStatusAndBossIdsContainingAll(
            @Param("worldGroup") EWorldGroup worldGroup,
            @Param("status") PostStatus status,
            @Param("bossIds") List<String> bossIds,
            @Param("bossIdCount") long bossIdCount,
            Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM PostJpaEntity p WHERE p.worldGroup = :worldGroup AND p.status = :status " +
           "AND (SELECT COUNT(DISTINCT b) FROM PostJpaEntity p2 JOIN p2.bossIds b " +
           "WHERE p2 = p AND b IN :bossIds) = :bossIdCount")
    long countByWorldGroupAndStatusAndBossIdsContainingAll(
            @Param("worldGroup") EWorldGroup worldGroup,
            @Param("status") PostStatus status,
            @Param("bossIds") List<String> bossIds,
            @Param("bossIdCount") long bossIdCount);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications a WHERE a.applicantId = :applicantId")
    List<PostJpaEntity> findByApplicationsApplicantId(@Param("applicantId") String applicantId);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications a WHERE a.applicantId = :applicantId AND a.status = :status")
    List<PostJpaEntity> findByApplicationsApplicantIdAndStatus(@Param("applicantId") String applicantId, @Param("status") ApplicationStatus status);
}
