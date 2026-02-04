package com.mapleraid.core.dto;

import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * SelfValidating 을 상속받아서 사용하는 클래스는
 * 해당 클래스가 만들어질 때 Validation 을 수행한다.
 *
 * @param <T>
 */
@Slf4j
public abstract class SelfValidating<T> {

    private static final Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Validator 초기화 실패");
        }
    }

    @SuppressWarnings("unchecked")
    protected void validateSelf() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            throw new CommonException(ErrorCode.INVALID_ARGUMENT, errorMessages);
        }
    }
}
