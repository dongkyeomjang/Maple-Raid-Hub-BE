package com.mapleraid.post.application.service;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.post.application.port.in.input.command.RejectApplicationInput;
import com.mapleraid.post.application.port.in.output.result.RejectApplicationResult;
import com.mapleraid.post.application.port.in.usecase.RejectApplicationUseCase;
import com.mapleraid.post.application.port.out.PostRepository;
import com.mapleraid.post.domain.Application;
import com.mapleraid.post.domain.Post;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class RejectApplicationService implements RejectApplicationUseCase {

    private final PostRepository postRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public RejectApplicationService(PostRepository postRepository,
                                    SimpMessagingTemplate messagingTemplate) {
        this.postRepository = postRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public RejectApplicationResult execute(RejectApplicationInput input) {
        Post post = postRepository.findByIdWithApplications(input.getPostId())
                .orElseThrow(() -> new CommonException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(input.getRequesterId())) {
            throw new CommonException(ErrorCode.POST_NOT_AUTHOR);
        }

        post.rejectApplication(input.getApplicationId());

        postRepository.save(post);

        Application rejectedApp = post.getApplications().stream()
                .filter(app -> app.getId().equals(input.getApplicationId()))
                .findFirst()
                .orElseThrow();

        messagingTemplate.convertAndSend(
                "/topic/post/" + input.getPostId().getValue(),
                Map.of("type", "APPLICATION_REJECTED",
                        "postId", input.getPostId().getValue().toString(),
                        "applicationId", input.getApplicationId().getValue().toString()));

        return RejectApplicationResult.from(rejectedApp);
    }
}
