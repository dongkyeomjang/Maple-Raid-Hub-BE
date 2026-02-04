package com.mapleraid.post.adapter.out.persistence;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.post.adapter.out.persistence.jpa.PostJpaEntity;
import com.mapleraid.post.adapter.out.persistence.jpa.PostJpaRepository;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.ApplicationStatus;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.post.domain.PostStatus;
import com.mapleraid.user.domain.UserId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostPersistenceAdapter implements PostRepository {

    private final PostJpaRepository jpaRepository;

    public PostPersistenceAdapter(PostJpaRepository jpaRepository) {
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
    public List<Post> findByWorldGroupAndStatus(EWorldGroup worldGroup, PostStatus status, int page, int size) {
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
    public long countByWorldGroupAndStatus(EWorldGroup worldGroup, PostStatus status) {
        return jpaRepository.countByWorldGroupAndStatus(worldGroup, status);
    }

    @Override
    public long countByStatus(PostStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Application> findApplicationsByApplicantId(UserId applicantId) {
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
