package com.mapleraid.adapter.out.persistence;

import com.mapleraid.adapter.out.persistence.entity.PostJpaEntity;
import com.mapleraid.adapter.out.persistence.repository.PostJpaRepository;
import com.mapleraid.application.port.out.PostRepository;
import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.ApplicationStatus;
import com.mapleraid.domain.post.Post;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.post.PostStatus;
import com.mapleraid.domain.user.UserId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository jpaRepository;

    public PostRepositoryAdapter(PostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = PostJpaEntity.fromDomain(post);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Post> findById(PostId id) {
        return jpaRepository.findById(id.getValue().toString())
                .map(PostJpaEntity::toDomain);
    }

    @Override
    public Optional<Post> findByIdWithApplications(PostId id) {
        return jpaRepository.findByIdWithApplications(id.getValue().toString())
                .map(PostJpaEntity::toDomain);
    }

    @Override
    public List<Post> findByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findByWorldGroupAndStatus(worldGroup, status, pageable).stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Post> findByStatus(PostStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findByStatus(status, pageable).stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status) {
        return jpaRepository.countByWorldGroupAndStatus(worldGroup, status);
    }

    @Override
    public long countByStatus(PostStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Application> findApplicationsByApplicantId(UserId applicantId) {
        // PENDING 상태의 지원만 조회 (ACCEPTED, REJECTED, WITHDRAWN, CANCELED는 마이페이지에서 숨김)
        return jpaRepository.findByApplicationsApplicantIdAndStatus(
                        applicantId.getValue().toString(), ApplicationStatus.APPLIED)
                .stream()
                .flatMap(entity -> entity.toDomain().getApplications().stream()
                        .filter(app -> app.getApplicantId().equals(applicantId)
                                && app.getStatus() == ApplicationStatus.APPLIED))
                .toList();
    }

    @Override
    public List<Post> findByAuthorId(UserId authorId) {
        // 모집 중인 글만 조회 (CLOSED, CANCELED, EXPIRED는 마이페이지에서 숨김)
        return jpaRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(
                        authorId.getValue().toString(), PostStatus.RECRUITING)
                .stream()
                .map(PostJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Post post) {
        jpaRepository.deleteById(post.getId().getValue().toString());
    }
}
