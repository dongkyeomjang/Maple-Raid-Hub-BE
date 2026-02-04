package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadMyPostsResult;

import java.util.List;

public record ReadMyPostsResponseDto(
        List<PostResponseDto> posts
) {
    public static ReadMyPostsResponseDto from(ReadMyPostsResult result) {
        List<PostResponseDto> posts = result.getPosts().stream()
                .map(PostResponseDto::from)
                .toList();
        return new ReadMyPostsResponseDto(posts);
    }
}
