package com.mapleraid.adapter.in.web.dto.party;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AllAvailabilityResponse(
        List<AvailabilityResponse> memberAvailabilities,
        List<HeatmapSlot> heatmap,
        LocalDate startDate,
        LocalDate endDate
) {
    /**
     * 히트맵 슬롯 - 특정 시간대에 몇 명이 가능한지
     */
    public record HeatmapSlot(
            LocalDate date,
            LocalTime time,
            int availableCount,
            List<String> availableUserIds
    ) {
    }
}
