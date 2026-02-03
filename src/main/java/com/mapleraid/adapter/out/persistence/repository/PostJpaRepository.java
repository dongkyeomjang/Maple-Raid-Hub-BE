package com.mapleraid.adapter.out.persistence.repository;

import com.mapleraid.adapter.out.persistence.entity.PostJpaEntity;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.post.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, String> {

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications WHERE p.id = :id")
    Optional<PostJpaEntity> findByIdWithApplications(@Param("id") String id);

    List<PostJpaEntity> findByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status, Pageable pageable);

    List<PostJpaEntity> findByStatus(PostStatus status, Pageable pageable);

    long countByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status);

    long countByStatus(PostStatus status);

    List<PostJpaEntity> findByAuthorId(String authorId);

    List<PostJpaEntity> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    List<PostJpaEntity> findByAuthorIdAndStatusOrderByCreatedAtDesc(String authorId, PostStatus status);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications a WHERE a.applicantId = :applicantId")
    List<PostJpaEntity> findByApplicationsApplicantId(@Param("applicantId") String applicantId);

    @Query("SELECT DISTINCT p FROM PostJpaEntity p LEFT JOIN FETCH p.applications a WHERE a.applicantId = :applicantId AND a.status = :status")
    List<PostJpaEntity> findByApplicationsApplicantIdAndStatus(@Param("applicantId") String applicantId, @Param("status") com.mapleraid.domain.post.ApplicationStatus status);
}
