package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.post.domain.PostStatus;
import lombok.Getter;

@Getter
public class ReadPostListInput {

    private final EWorldGroup worldGroup;
    private final PostStatus status;
    private final int page;
    private final int size;

    private ReadPostListInput(EWorldGroup worldGroup, PostStatus status, int page, int size) {
        this.worldGroup = worldGroup;
        this.status = status;
        this.page = page;
        this.size = size;
    }

    public static ReadPostListInput of(EWorldGroup worldGroup, PostStatus status, int page, int size) {
        return new ReadPostListInput(worldGroup, status, page, size);
    }
}
