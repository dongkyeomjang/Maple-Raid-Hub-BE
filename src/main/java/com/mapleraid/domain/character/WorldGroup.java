package com.mapleraid.domain.character;

/**
 * 월드 그룹 - 크로스월드 파티 매칭 가능 그룹
 */
public enum WorldGroup {
    CHALLENGER("챌린저스 그룹"),
    EOS_HELIOS("에오스-헬리오스 그룹"),
    NORMAL("일반 월드 그룹");

    private final String displayName;

    WorldGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
