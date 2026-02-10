package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadMyApplicationsResult;

import java.util.List;

public record MyApplicationsListResponseDto(
        List<MyApplicationResponseDto> applications
) {
    public static MyApplicationsListResponseDto from(ReadMyApplicationsResult result) {
        List<MyApplicationResponseDto> applications = result.getApplications().stream()
                .map(MyApplicationResponseDto::from)
                .toList();
        return new MyApplicationsListResponseDto(applications);
    }
}
