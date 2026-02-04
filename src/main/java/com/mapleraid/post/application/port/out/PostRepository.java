package com.mapleraid.post.application.port.out;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import com.mapleraid.post.domain.PostId;
import com.mapleraid.post.domain.PostStatus;
import com.mapleraid.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(PostId id);

    Optional<Post> findByIdWithApplications(PostId id);

    List<Post> findByStatus(PostStatus status, int page, int size);

    List<Post> findByWorldGroupAndStatus(EWorldGroup EWorldGroup, PostStatus status, int page, int size);

    long countByStatus(PostStatus status);

    long countByWorldGroupAndStatus(EWorldGroup EWorldGroup, PostStatus status);

    List<Application> findApplicationsByApplicantId(UserId applicantId);

    List<Post> findByAuthorId(UserId authorId);

    void delete(Post post);
}
