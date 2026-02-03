package com.mapleraid.adapter.in.web.dto;

/**
 * 프론트엔드용 보스 설정 DTO
 */
public record BossConfigDto(
        String id,
        String bossFamily,
        String name,
        String shortName,
        String difficulty,
        int partySize,
        String resetType,
        String iconUrl
) {
    public static BossConfigDto from(
            String id,
            String bossFamily,
            String displayName,
            String shortName,
            String difficulty,
            int maxPartySize,
            boolean weeklyReset,
            boolean monthlyReset
    ) {
        String resetType = monthlyReset ? "MONTHLY" : (weeklyReset ? "WEEKLY" : "DAILY");
        return new BossConfigDto(
                id,
                bossFamily,
                displayName,
                shortName,
                difficulty,
                maxPartySize,
                resetType,
                null
        );
    }
}
