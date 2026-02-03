package com.mapleraid.domain.character;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 월드명을 월드 그룹으로 매핑하는 도메인 서비스
 */
@Service
public class WorldGroupResolver {

    private static final Set<String> CHALLENGER_WORLDS = Set.of(
            "챌린저스", "챌린저스2", "챌린저스3", "챌린저스4"
    );

    private static final Set<String> EOS_HELIOS_WORLDS = Set.of(
            "에오스", "헬리오스"
    );

    private static final Set<String> NORMAL_WORLDS = Set.of(
            "스카니아", "베라", "루나", "제니스", "크로아", "유니온", "엘리시움", "이노시스",
            "레드", "오로라", "아케인", "노바"
    );

    /**
     * 월드명으로 월드 그룹 조회
     */
    public WorldGroup resolve(String worldName) {
        if (CHALLENGER_WORLDS.contains(worldName)) {
            return WorldGroup.CHALLENGER;
        }
        if (EOS_HELIOS_WORLDS.contains(worldName)) {
            return WorldGroup.EOS_HELIOS;
        }
        if (NORMAL_WORLDS.contains(worldName)) {
            return WorldGroup.NORMAL;
        }
        // 알 수 없는 월드는 NORMAL로 기본 처리
        return WorldGroup.NORMAL;
    }

    /**
     * 두 월드가 같은 그룹인지 확인
     */
    public boolean isSameGroup(String world1, String world2) {
        return resolve(world1) == resolve(world2);
    }

    /**
     * 두 월드 그룹이 매칭 가능한지 확인
     */
    public boolean canMatch(WorldGroup group1, WorldGroup group2) {
        return group1 == group2;
    }

    /**
     * 월드 그룹에 포함된 모든 월드 목록
     */
    public Set<String> getWorldsInGroup(WorldGroup group) {
        return switch (group) {
            case CHALLENGER -> CHALLENGER_WORLDS;
            case EOS_HELIOS -> EOS_HELIOS_WORLDS;
            case NORMAL -> NORMAL_WORLDS;
        };
    }

    /**
     * 월드 유효성 검증
     */
    public boolean isValidWorld(String worldName) {
        return CHALLENGER_WORLDS.contains(worldName)
                || EOS_HELIOS_WORLDS.contains(worldName)
                || NORMAL_WORLDS.contains(worldName);
    }
}
