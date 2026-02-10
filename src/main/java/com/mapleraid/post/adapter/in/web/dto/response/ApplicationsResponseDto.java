package com.mapleraid.post.adapter.in.web.dto.response;

import com.mapleraid.post.application.port.in.output.result.ReadPostApplicationsResult;

import java.util.List;

public record ApplicationsResponseDto(
        List<ApplicationResponseDto> applications
) {
    public static ApplicationsResponseDto from(ReadPostApplicationsResult result) {
        List<ApplicationResponseDto> applications = result.getApplications().stream()
                .map(ApplicationResponseDto::from)
                .toList();
        return new ApplicationsResponseDto(applications);
    }
}
