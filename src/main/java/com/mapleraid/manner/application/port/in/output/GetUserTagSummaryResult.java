package com.mapleraid.manner.application.port.in.output;

import lombok.Getter;

import java.util.List;

@Getter
public class GetUserTagSummaryResult {

    private final List<TagCount> tagCounts;
    private final int totalEvaluations;
    private final double temperature;

    public GetUserTagSummaryResult(List<TagCount> tagCounts, int totalEvaluations, double temperature) {
        this.tagCounts = tagCounts;
        this.totalEvaluations = totalEvaluations;
        this.temperature = temperature;
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
