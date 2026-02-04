package com.mapleraid.user.adapter.in.web.query;

import com.mapleraid.core.dto.ResponseDto;
import com.mapleraid.user.adapter.out.persistence.jpa.UserJpaRepository;
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
    public ResponseDto<SiteStatsResponse> getSiteStats() {
        long userCount = userJpaRepository.count();
        BigDecimal avgTemp = userJpaRepository.findAverageTemperature();

        double averageTemperature = avgTemp != null
                ? avgTemp.setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 36.5;

        return ResponseDto.ok(new SiteStatsResponse(userCount, averageTemperature));
    }

    public record SiteStatsResponse(
            long userCount,
            double averageTemperature
    ) {
    }
}
