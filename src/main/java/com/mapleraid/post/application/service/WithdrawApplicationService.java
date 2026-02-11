package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.WithdrawApplicationInput;
import com.mapleraid.post.application.port.in.output.result.WithdrawApplicationResult;
import com.mapleraid.post.application.port.in.usecase.WithdrawApplicationUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WithdrawApplicationService implements WithdrawApplicationUseCase {

    private final PostRepository postRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public WithdrawApplicationResult execute(WithdrawApplicationInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        post.withdrawApplication(input.getApplicationId(), input.getRequesterId());

        postRepository.save(post);

        Application withdrawnApp = post.getApplications().stream()
                .filter(app -> app.getId().equals(input.getApplicationId()))
                .findFirst()
                .orElseThrow();

        messagingTemplate.convertAndSend(
                "/topic/post/" + input.getPostId().getValue(),
                Map.of("type", "APPLICATION_WITHDRAWN",
                        "postId", input.getPostId().getValue().toString(),
                        "applicationId", input.getApplicationId().getValue().toString()));

        return WithdrawApplicationResult.from(withdrawnApp);
    }
}
