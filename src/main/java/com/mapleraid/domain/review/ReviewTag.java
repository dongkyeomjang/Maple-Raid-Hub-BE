package com.mapleraid.domain.review;

import java.math.BigDecimal;

/**
 * 리뷰 태그 및 온도 영향
 */
public enum ReviewTag {
    // 긍정 태그
    SKILLED("실력이 좋아요", Category.POSITIVE, new BigDecimal("0.5")),
    PUNCTUAL("시간 약속을 잘 지켜요", Category.POSITIVE, new BigDecimal("0.4")),
    GOOD_COMM("소통이 원활해요", Category.POSITIVE, new BigDecimal("0.4")),
    HELPFUL("파티원을 잘 도와줘요", Category.POSITIVE, new BigDecimal("0.4")),
    PATIENT("인내심이 있어요", Category.POSITIVE, new BigDecimal("0.3")),
    LEADER("리더십이 좋아요", Category.POSITIVE, new BigDecimal("0.5")),
    PREPARED("사전 준비를 잘 해와요", Category.POSITIVE, new BigDecimal("0.3")),
    POSITIVE("긍정적인 분위기를 만들어요", Category.POSITIVE, new BigDecimal("0.3")),

    // 중립 태그
    NORMAL("평범했어요", Category.NEUTRAL, BigDecimal.ZERO),
    QUIET("조용했어요", Category.NEUTRAL, BigDecimal.ZERO),
    FIRST_TIMER("첫 파티였어요", Category.NEUTRAL, BigDecimal.ZERO),

    // 부정 태그
    LATE("시간 약속을 안 지켜요", Category.NEGATIVE, new BigDecimal("-0.3")),
    POOR_COMM("소통이 어려워요", Category.NEGATIVE, new BigDecimal("-0.3")),
    UNPREPARED("준비 없이 왔어요", Category.NEGATIVE, new BigDecimal("-0.3")),
    RUDE("불쾌한 언행이 있었어요", Category.NEGATIVE, new BigDecimal("-0.5")),
    CARRIED("실력이 기대 이하였어요", Category.NEGATIVE, new BigDecimal("-0.3")),
    NO_SHOW("노쇼했어요", Category.NEGATIVE, new BigDecimal("-1.0")),
    LEFT_EARLY("중간에 나갔어요", Category.NEGATIVE, new BigDecimal("-0.5"));

    private final String displayName;
    private final Category category;
    private final BigDecimal temperatureEffect;

    ReviewTag(String displayName, Category category, BigDecimal temperatureEffect) {
        this.displayName = displayName;
        this.category = category;
        this.temperatureEffect = temperatureEffect;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getTemperatureEffect() {
        return temperatureEffect;
    }

    public boolean isPositive() {
        return category == Category.POSITIVE;
    }

    public boolean isNegative() {
        return category == Category.NEGATIVE;
    }

    public enum Category {
        POSITIVE, NEUTRAL, NEGATIVE
    }
}
