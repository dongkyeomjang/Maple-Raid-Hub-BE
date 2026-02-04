package com.mapleraid.post.adapter.in.web.dto.response;

import java.util.List;

public record PostListResponseDto(
        List<PostResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static PostListResponseDto of(List<PostResponseDto> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        return new PostListResponseDto(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }
}
