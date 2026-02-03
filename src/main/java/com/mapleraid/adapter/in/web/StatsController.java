package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final UserJpaRepository userJpaRepository;

    public StatsController(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SiteStatsResponse>> getSiteStats() {
        long userCount = userJpaRepository.count();
        BigDecimal avgTemp = userJpaRepository.findAverageTemperature();

        double averageTemperature = avgTemp != null
                ? avgTemp.setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 36.5;

        return ResponseEntity.ok(ApiResponse.success(new SiteStatsResponse(userCount, averageTemperature)));
    }

    public record SiteStatsResponse(
            long userCount,
            double averageTemperature
    ) {
    }
}
