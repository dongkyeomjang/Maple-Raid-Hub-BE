package com.mapleraid.domain.common;

import java.util.Objects;
import java.util.UUID;

/**
 * 도메인 ID 값 객체 기본 클래스
 */
public abstract class BaseId<T> {
    protected final T value;

    protected BaseId(T value) {
        this.value = Objects.requireNonNull(value, "ID value cannot be null");
    }

    protected static UUID generateUUID() {
        return UUID.randomUUID();
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseId<?> baseId = (BaseId<?>) o;
        return Objects.equals(value, baseId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
