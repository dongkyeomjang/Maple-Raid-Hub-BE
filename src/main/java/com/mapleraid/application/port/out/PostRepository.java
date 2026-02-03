package com.mapleraid.application.port.out;

import com.mapleraid.domain.character.WorldGroup;
import com.mapleraid.domain.post.Application;
import com.mapleraid.domain.post.Post;
import com.mapleraid.domain.post.PostId;
import com.mapleraid.domain.post.PostStatus;
import com.mapleraid.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(PostId id);

    Optional<Post> findByIdWithApplications(PostId id);

    List<Post> findByStatus(PostStatus status, int page, int size);

    List<Post> findByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status, int page, int size);

    long countByStatus(PostStatus status);

    long countByWorldGroupAndStatus(WorldGroup worldGroup, PostStatus status);

    List<Application> findApplicationsByApplicantId(UserId applicantId);

    List<Post> findByAuthorId(UserId authorId);

    void delete(Post post);
}
