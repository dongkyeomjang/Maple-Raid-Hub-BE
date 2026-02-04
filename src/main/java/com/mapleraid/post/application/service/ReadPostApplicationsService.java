package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.query.ReadPostApplicationsInput;
import com.mapleraid.post.application.port.in.output.result.ReadPostApplicationsResult;
import com.mapleraid.post.application.port.in.usecase.ReadPostApplicationsUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReadPostApplicationsService implements ReadPostApplicationsUseCase {

    private final PostRepository postRepository;

    public ReadPostApplicationsService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public ReadPostApplicationsResult execute(ReadPostApplicationsInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        List<ReadPostApplicationsResult.ApplicationSummary> summaries = post.getApplications().stream()
                .map(ReadPostApplicationsResult.ApplicationSummary::from)
                .toList();

        return new ReadPostApplicationsResult(summaries);
    }
}
