package com.mapleraid.manner.application.port.in.output;

import lombok.Getter;

import java.util.List;

@Getter
public class GetUserTagSummaryResult {

    private final List<TagCount> tagCounts;
    private final int totalEvaluations;

    public GetUserTagSummaryResult(List<TagCount> tagCounts, int totalEvaluations) {
        this.tagCounts = tagCounts;
        this.totalEvaluations = totalEvaluations;
    }

    @Getter
    public static class TagCount {
        private final String tag;
        private final int count;

        public TagCount(String tag, int count) {
            this.tag = tag;
            this.count = count;
        }
    }
}
