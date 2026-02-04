package com.mapleraid.post.application.service;

import com.mapleraid.post.application.port.in.input.query.ReadMyApplicationsInput;
import com.mapleraid.post.application.port.in.output.result.ReadMyApplicationsResult;
import com.mapleraid.post.application.port.in.usecase.ReadMyApplicationsUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReadMyApplicationsService implements ReadMyApplicationsUseCase {

    private final PostRepository postRepository;

    public ReadMyApplicationsService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public ReadMyApplicationsResult execute(ReadMyApplicationsInput input) {
        List<Application> applications = postRepository.findApplicationsByApplicantId(input.getUserId());

        List<ReadMyApplicationsResult.ApplicationSummary> summaries = applications.stream()
                .map(ReadMyApplicationsResult.ApplicationSummary::from)
                .toList();

        return new ReadMyApplicationsResult(summaries);
    }
}
