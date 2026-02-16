package com.mapleraid.manner.domain;

import java.math.BigDecimal;

public enum MannerTag {
    // 긍정 태그 (가중치 동일: 1.0)
    GOOD_CONTACT("연락이 잘돼요", Category.POSITIVE, new BigDecimal("1.0")),
    PUNCTUAL("시간 약속을 잘 지켜요", Category.POSITIVE, new BigDecimal("1.0")),
    KIND("친절하고 매너가 좋아요", Category.POSITIVE, new BigDecimal("1.0")),
    CARRIES_LOG("통나무를 들어줘요", Category.POSITIVE, new BigDecimal("1.0")),
    GOOD_CONTROL("컨트롤이 좋아요", Category.POSITIVE, new BigDecimal("1.0")),

    // 부정 태그 (가중치 동일: 1.5, 부정이 더 무겁게)
    BAD_CONTACT("연락이 잘 안돼요", Category.NEGATIVE, new BigDecimal("1.5")),
    LATE("시간 약속을 안 지켜요", Category.NEGATIVE, new BigDecimal("1.5")),
    NO_SHOW("노쇼를 했어요", Category.NEGATIVE, new BigDecimal("1.5")),
    RUDE("불친절해요", Category.NEGATIVE, new BigDecimal("1.5")),
    TOXIC("비매너 언행이 있었어요", Category.NEGATIVE, new BigDecimal("1.5"));

    private final String displayName;
    private final Category category;
    private final BigDecimal weight;

    MannerTag(String displayName, Category category, BigDecimal weight) {
        this.displayName = displayName;
        this.category = category;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

    /**
     * 태그의 가중치 (항상 양수). 비율 계산에 사용.
     */
    public BigDecimal getWeight() {
        return weight;
    }

    /**
     * @deprecated Use getWeight() instead. Kept for backward compatibility with existing evaluation records.
     */
    @Deprecated
    public BigDecimal getTemperatureEffect() {
        return isPositive() ? weight : weight.negate();
    }

    public boolean isPositive() {
        return category == Category.POSITIVE;
    }

    public boolean isNegative() {
        return category == Category.NEGATIVE;
    }

    public enum Category {
        POSITIVE, NEGATIVE
    }
}
