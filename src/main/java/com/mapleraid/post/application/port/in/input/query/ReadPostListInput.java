package com.mapleraid.post.application.port.in.input.query;

import com.mapleraid.character.domain.type.EWorldGroup;
import com.mapleraid.post.domain.PostStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class ReadPostListInput {

    private final EWorldGroup worldGroup;
    private final PostStatus status;
    private final int page;
    private final int size;
    private final List<String> bossIds;

    private ReadPostListInput(EWorldGroup worldGroup, PostStatus status, int page, int size, List<String> bossIds) {
        this.worldGroup = worldGroup;
        this.status = status;
        this.page = page;
        this.size = size;
        this.bossIds = bossIds;
    }

    public static ReadPostListInput of(EWorldGroup worldGroup, PostStatus status, int page, int size) {
        return new ReadPostListInput(worldGroup, status, page, size, null);
    }

    public static ReadPostListInput of(EWorldGroup worldGroup, PostStatus status, int page, int size, List<String> bossIds) {
        return new ReadPostListInput(worldGroup, status, page, size, bossIds);
    }
}
