package com.mapleraid.adapter.in.web.dto;

import java.util.List;

/**
 * 프론트엔드용 월드 그룹 설정 DTO
 */
public record WorldGroupConfigDto(
        String id,
        String displayName,
        List<String> worlds
) {
    public static WorldGroupConfigDto from(String id, String displayName, List<String> worldNames) {
        return new WorldGroupConfigDto(id, displayName, worldNames);
    }
}
