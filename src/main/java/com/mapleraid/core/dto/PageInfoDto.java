package com.mapleraid.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PageInfoDto extends SelfValidating<PageInfoDto> {

    @NotNull(message = "currentPage는 필수 값입니다.")
    @Min(value = 1, message = "currentPage는 1 이상이어야 합니다.")
    private final Integer currentPage;

    @NotNull(message = "pageSize는 필수 값입니다.")
    @Min(value = 1, message = "pageSize는 1 이상이어야 합니다.")
    private final Integer pageSize;

    @NotNull(message = "totalPages는 필수 값입니다.")
    @Min(value = 0, message = "totalPages는 0 이상이어야 합니다.")
    private final Integer totalPages;

    @NotNull(message = "totalItems는 필수 값입니다.")
    @Min(value = 0, message = "totalItems는 0 이상이어야 합니다.")
    private final Long totalItems;

    public PageInfoDto(
            Integer currentPage,
            Integer pageSize,
            Integer totalPages,
            Long totalItems
    ) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.validateSelf();
    }

    public static PageInfoDto of(
            Integer currentPage,
            Integer pageSize,
            Integer totalPages,
            Long totalItems
    ) {
        return new PageInfoDto(
                currentPage,
                pageSize,
                totalPages,
                totalItems
        );
    }
}
