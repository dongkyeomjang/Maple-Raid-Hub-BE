package com.mapleraid.manner.adapter.in.web.dto;

import com.mapleraid.manner.application.port.in.output.GetUserTagSummaryResult;

import java.util.List;

public record TagSummaryDto(
        List<TagCountDto> tagCounts,
        int totalEvaluations,
        double temperature
) {
    public static TagSummaryDto from(GetUserTagSummaryResult result) {
        return new TagSummaryDto(
                result.getTagCounts().stream()
                        .map(tc -> new TagCountDto(tc.getTag(), tc.getCount()))
                        .toList(),
                result.getTotalEvaluations(),
                result.getTemperature()
        );
    }

    public record TagCountDto(
            String tag,
            int count
    ) {
    }
}
